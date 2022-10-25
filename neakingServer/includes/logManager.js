const LogsData =require("./../models/Logs")

const log = async (type, message,userName,clientId) => {
    console.log(type.name, message,clientId);
    await LogsData.updateOne(
        { user:  userName },
        {
             $push:{logs:{type,message,time:new Date(),clientId}},
        },
        { upsert: true }
    )
}

module.exports = {
    log,
    getLogs: async (userName) => {
        let logs=await LogsData.findOne({user:userName}).sort({ time: 1 }).lean()
        if(logs){
            logs= logs.logs
        }else {
            logs=[]
        }
       return logs;
      //  return db.maindb.get('admin.logs').sortBy('time').reverse().value();
    }
}
