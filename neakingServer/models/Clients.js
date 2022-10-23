// Require Dependencies
const mongoose = require('mongoose');
const SchemaTypes = mongoose.Schema.Types;

// Setup  Schema
const ClientsSchema = new mongoose.Schema({
  // Basic fields

  user:{
    type: String,
  },
  clientID: String,
  firstSeen: {
    type: Date,
    default: Date.now,
  },
  lastSeen: {
    type: Date,
    default: Date.now,
  },
  isOnline: {
    type: Boolean,
    default: true,
  },
  dynamicData: {
    type: Object,
    default: {},
  },

  // When this user was created
  created: {
    type: Date,
    default: Date.now,
  },
});

// Create and export the new model
const Clients = (module.exports = mongoose.model('Clients', ClientsSchema));
