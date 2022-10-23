// Require Dependencies
const mongoose = require('mongoose');
const SchemaTypes = mongoose.Schema.Types;

// Setup  Schema
const LogsSchema = new mongoose.Schema({
  // Basic fields

  logs: Object,
  user:String,

  // When this was created

  created: {
    type: Date,
    default: Date.now,
  },
});

// Create and export the new model
const Logs = (module.exports = mongoose.model('Logs', LogsSchema));
