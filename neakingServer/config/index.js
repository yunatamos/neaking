// Application Main Config
module.exports = {

  database: {
    developmentMongoURI: "mongodb://127.0.0.1:27017/neaking", // MongoURI to use in development
    productionMongoURI: "mongodb://127.0.0.1:27017/neaking", // MongoURI to use in production
  },
  authentication: {
    jwtSecret: "JU?uRU25=DDWkPGutzGVz@&AK5-a5Rz?zUT+TF_c%MF%@6@JGyyhR+-#gh7jguhnfgy7678678ujEq?CCsGkC^d!tPqVy^4?d%EXbk_qY4uNV+HZmRTQ+=ynfgtk8VWDGr#CUEnREQQtQjN#hrury6u67u768678hg", // Secret used to sign JWT's. KEEP THIS AS A SECRET
    jwtExpirationTime: 360000, // JWT-token expiration time (in seconds)
  }
};
