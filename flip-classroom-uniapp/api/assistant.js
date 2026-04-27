import config from '@/common/config'

const AI_OFFLINE_MESSAGE = 'AI 助手暂时不可用，请确认 Python 后端已在 8000 端口启动。'

function normalizeAiError(error) {
  if (error && error.message) {
    return error
  }
  return new Error(AI_OFFLINE_MESSAGE)
}

function emitSSEPayload(payload, callbacks) {
  if (!payload) return

  if (payload === '[DONE]') {
    if (callbacks.onDone) callbacks.onDone()
    return
  }

  try {
    const parsed = JSON.parse(payload)
    if (parsed.error) {
      const error = new Error(parsed.error || AI_OFFLINE_MESSAGE)
      if (callbacks.onError) callbacks.onError(error)
      return
    }
    if (callbacks.onChunk) callbacks.onChunk(parsed)
  } catch (error) {
    if (callbacks.onError) callbacks.onError(error)
  }
}

function createSSEParser(callbacks) {
  let buffer = ''

  return function parseChunk(text) {
    buffer += text
    const events = buffer.split('\n\n')
    buffer = events.pop() || ''

    events.forEach((eventBlock) => {
      eventBlock
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)
        .forEach((line) => {
          if (line.startsWith('data: ')) {
            emitSSEPayload(line.slice(6), callbacks)
          }
        })
    })
  }
}

function buildAssistantParams(options) {
  return {
    query: options.question,
    user_id: options.userId,
    topic_id: options.topicId || 0,
    image_url: options.imageUrl || '',
    websearch: options.websearch ? 1 : 0,
    deepthink: options.deepthink ? 1 : 0,
    save_history: options.saveHistory === false ? 0 : 1,
    use_rag: options.useRag ? 1 : 0
  }
}

function decodeChunkData(data) {
  if (typeof data === 'string') return data

  try {
    if (typeof TextDecoder !== 'undefined') {
      return new TextDecoder('utf-8').decode(new Uint8Array(data))
    }
  } catch (error) {
    console.error('AI 流式响应解码失败', error)
  }

  const bytes = new Uint8Array(data)
  let text = ''
  bytes.forEach((byte) => {
    text += String.fromCharCode(byte)
  })
  try {
    return decodeURIComponent(escape(text))
  } catch (error) {
    return text
  }
}

async function sendByFetchStream(options, parser) {
  const params = new URLSearchParams()
  const requestParams = buildAssistantParams(options)
  Object.keys(requestParams).forEach((key) => {
    params.append(key, requestParams[key])
  })

  const response = await fetch(`${config.aiBaseURL}/ai/assistant-stream/?${params.toString()}`)
  if (!response.ok) {
    throw new Error(AI_OFFLINE_MESSAGE)
  }

  if (!response.body || !response.body.getReader) {
    const text = await response.text()
    parser(text)
    return response
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    parser(decoder.decode(value, { stream: true }))
  }
  parser(decoder.decode())
  return response
}

export function uploadStudyImage(filePath) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${config.aiBaseURL}/ai/upload/`,
      filePath,
      name: 'image',
      success: (response) => {
        try {
          const body = JSON.parse(response.data || '{}')
          if (body.url) {
            resolve({
              relativeUrl: body.url,
              fullUrl: `${config.aiBaseURL}${body.url}`,
              filename: body.filename || ''
            })
            return
          }
          reject(new Error(body.error || '图片上传失败'))
        } catch (error) {
          reject(error)
        }
      },
      fail: () => reject(new Error(AI_OFFLINE_MESSAGE))
    })
  })
}

export function sendStudyAssistantStream(options) {
  const parser = createSSEParser({
    onChunk: options.onChunk,
    onDone: options.onDone,
    onError: options.onError
  })

  if (typeof fetch === 'function' && typeof ReadableStream !== 'undefined') {
    return sendByFetchStream(options, parser).catch((error) => {
      const normalized = normalizeAiError(error)
      if (options.onError) options.onError(normalized)
      throw normalized
    })
  }

  return new Promise((resolve, reject) => {
    const requestTask = uni.request({
      url: `${config.aiBaseURL}/ai/assistant-stream/`,
      method: 'GET',
      data: buildAssistantParams(options),
      enableChunked: true,
      responseType: 'text',
      success: (response) => {
        if (typeof response.data === 'string' && response.data) {
          parser(response.data)
        }
        resolve(response)
      },
      fail: (error) => {
        const normalized = normalizeAiError(error)
        if (options.onError) options.onError(normalized)
        reject(normalized)
      }
    })

    if (requestTask && requestTask.onChunkReceived) {
      requestTask.onChunkReceived((response) => {
        parser(decodeChunkData(response.data))
      })
    }
  })
}
