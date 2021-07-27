var express = require('express');
var app = express();

//setting middleware
app.use(express.static(__dirname + '/resources/public')); //Serves resources from public folder

app.get('/*', function(req, res){
  res.sendFile('index.html', { root: __dirname + '/resources/public'});
});

var server = app.listen(5000, () => {
  console.log('Listening ...');
  console.log('Address: ' + server.address().address);
  console.log('Port: ' + server.address().port);
});

