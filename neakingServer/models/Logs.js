// Require Dependencies
const mongoose = require('mongoose');

// Setup  Schema
const LogsSchema = new mongoose.Schema({
  // Basic fields

  logs: Object,
  user:String,
  clientId:String,

  // When this was created

  created: {
    type: Date,
    default: Date.now,
  },
});

// Create and export the new model
(module.exports = mongoose.model('Logs', LogsSchema));
