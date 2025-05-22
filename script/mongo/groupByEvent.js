var db = db.getSiblingDB('mcd');
print("Current DB:"+ db);
var output = db.getCollection('pos_event').aggregate(
  [
    {
      $group: {
        _id: '$event.Type',
        count: { $sum: 1 }
      }
    }
  ],
  { maxTimeMS: 60000, allowDiskUse: true }
);

output.forEach(printjson)