'use strict';function processData(a){a=require("cluster");var c=2*require("os").cpus().length;require("fs");var b=parseInt(process.argv[2])||200;0!=b%8&&(console.error("d must be multiple of 8"),process.exit(-1));0!=b*b/c%8&&(console.error("cannot distribute equal across cpus"),process.exit(-1));if(a.isMaster)for(var f=c,e=Array(c),q=0;q<c;q++)a.fork().on("message",function(a){e[this.id-1]=new Buffer(a.data);this.kill();f--;if(0==f)for(process.stdout.write("P4\n"+b+" "+b+"\n"),a=0;a<c;a++)process.stdout.write(e[a])});
else if(a.isWorker){a=a.worker.id;var t=Math.floor((a-1)*b/c),u=Math.floor(a*b/c),d=0,m=0,r=new Buffer(b*b/8/c);(function(){for(var a=2/b,c=0,n=t;n<u;n++)for(var f=2*n/b-1,p=0;p<b;p++){for(var g=0,h=0,k=0,l=0,e=0;50>e&&4>=k+l;e++)h=2*g*h+f,g=k-l+(a*p-1.5),k=g*g,l=h*h;d|=4>=k+l;m++;8===m?(r[c++]=d,m=d=0):d<<=1}})();process.send(r)}}process.stdin.resume();process.stdin.setEncoding("utf-8");var _input="";process.stdin.on("data",function(a){_input+=a});
process.stdin.on("end",function(){var a=new Date;processData(_input);console.log("Time Elapsed:"+(new Date-a))});