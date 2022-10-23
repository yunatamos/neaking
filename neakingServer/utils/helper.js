
function sleep (ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function time () {
  return Math.floor(Date.now() / 1000)
}

function strtotime (string) {
  return Math.floor(Date.parse(string) / 1000)
}

module.exports ={time, sleep, strtotime}
