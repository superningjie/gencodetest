import postal from 'postal'

// 这里的model是由createCustomModel返回的model
const pub = function (model, topic, data, callback) {
  const schemaId = model.schemaId
  const key = model.key
  const pageId = model.pageId
  postal.publish(
    {
      channel: `${pageId}_${schemaId}_${key}`,
      topic: topic,
      data
    },
    callback
  )
}

  // 这里的model是由createCustomModel返回的model
const sub = function (model, topic, callback) {
  const schemaId = model.schemaId
  const key = model.key
  const pageId = model.pageId
  const handler = postal.subscribe({
    channel: `${pageId}_${schemaId}_${key}`,
    topic: topic,
    callback
  })
  return handler
}

const unsub = function (handler) {
  postal.unsubscribe(handler)
}

export default {
  pub,
  sub,
  unsub
}