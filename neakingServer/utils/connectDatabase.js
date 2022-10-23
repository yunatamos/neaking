// Require Dependencies
const config = require('../config');
const mongoose = require('mongoose');
// Setup Additional Variables
const MONGO_URI =
  process.env.NODE_ENV === 'production' ?
    config.database.productionMongoURI :
    config.database.developmentMongoURI;

// Configure MongoDB and Mongoose
exports.connectDatabase = async function() {
  try {
    await mongoose
        .connect(MONGO_URI, {
          useNewUrlParser: true,
          useCreateIndex: true,
          useFindAndModify: false,
          useUnifiedTopology: true,
        })
        .then(async () => {
          console.log('MongoDB >> Connected!');

        },
        );
  } catch (error) {
    console.log(`MongoDB ERROR >> ${error.message}`);

    // Exit current process with failure
    process.exit(1);
  }

};


