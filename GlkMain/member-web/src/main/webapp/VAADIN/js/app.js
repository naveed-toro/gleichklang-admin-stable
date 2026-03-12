//webkitURL is deprecated but nevertheless
URL = window.URL || window.webkitURL;

var gumStream; 						//stream from getUserMedia()
var recorder; 						//WebAudioRecorder object
var input; 							//MediaStreamAudioSourceNode  we'll be recording
var encodingType; 					//holds selected encoding for resulting audio (file)
var encodeAfterRecord = true;       // when to encode

// shim for AudioContext when it's not avb. 
var AudioContext = window.AudioContext || window.webkitAudioContext;
var audioContext; //new audio context to help us record

var recordButton = document.getElementById("recordButton");
var stopButton = document.getElementById("stopButton");
var userId = document.getElementById("record").value;
var serverPath = document.getElementById("server").value;

//add events to those 2 buttons
recordButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);
        var lang = navigator.language || navigator.userLanguage;
        var iOS = !!navigator.platform && /iPad|iPhone|iPod/.test(navigator.platform);
        var isSafari = navigator.vendor && navigator.vendor.indexOf('Apple') > -1 &&
                       navigator.userAgent &&
                       navigator.userAgent.indexOf('CriOS') == -1 &&
                       navigator.userAgent.indexOf('FxiOS') == -1;
        if(iOS && !isSafari){
            if(lang = "en-us"){
          alert('Audio recording supported in safari only for iOS devices' );
          recordButton.disabled=true;
          }
          else{
          alert('Audioaufnahme wird in Safari nur für iOS-Geräte unterstützt' );
          recordButton.disabled=true;
          }
        }


function startRecording() {
	console.log("startRecording() called");
    document.getElementById("recordingsList").innerHTML = " ";
    var constraints = { audio: true, video:false }

	navigator.mediaDevices.getUserMedia(constraints).then(function(stream) {

	audioContext = new AudioContext();

		document.getElementById("image").innerHTML='<img src="VAADIN/themes/gk_theme/img/audiogif.gif"/>';
		gumStream = stream;

		input = audioContext.createMediaStreamSource(stream);

		recorder = new WebAudioRecorder(input, {
		  workerDir: "VAADIN/js/", // must end with slash
		  encoding: 'mp3',
		  numChannels:2, //2 is the default, mp3 encoding supports only 2
		  onEncoderLoading: function(recorder, encoding) {
		    },
		  onEncoderLoaded: function(recorder, encoding) {
		    }
		});

		recorder.onComplete = function(recorder, blob) {
		    createDownloadLink(blob,recorder.encoding);
		}

		recorder.setOptions({
		  timeLimit:300,
		  encodeAfterRecord:encodeAfterRecord,
	      ogg: {quality: 0.5},
	      mp3: {bitRate: 160}
	    });

		recorder.startRecording();


//		__log("Aufnahme hat begonnen, bitte sprechen Sie jetzt!");
//		__log(document.getElementById("image").src="audio.jpeg");

	}).catch(function(err) {
	    alert(err);
		recordButton.disabled = false;
    	stopButton.disabled = true;

	});

	recordButton.disabled = true;
    stopButton.disabled = false;
}

function stopRecording() {
	console.log("stopRecording() called");
    document.getElementById("image").innerHTML = " ";
	gumStream.getAudioTracks()[0].stop();

	stopButton.disabled = true;
	recordButton.disabled = false;

	recorder.finishRecording();
	if(lang = "en-us"){
	  recordButton.innerHTML="Record Again"
    }
    else{
      recordButton.innerHTML="Erneut aufnehmen"
    }
}

function createDownloadLink(blob,encoding) {

	var url = URL.createObjectURL(blob);
	var au = document.createElement('audio');
	var li = document.createElement('li');
	var link = document.createElement('a');

	//add controls to the <audio> element
	au.controls = true;
	au.src = url;

	//link the a element to the blob
	link.href = url;
	link.download = new Date().toISOString() + '.'+encoding;
	link.innerHTML = "save to disk";

	//add the new audio and a elements to the li element
	li.appendChild(au);
	//li.appendChild(link);
//    recordButton.disabled= true;
    var filename = new Date().getTime()+ '.'+encoding;

    var upload = document.createElement('button');
    upload.innerHTML ="Benutzen und hochladen"
    upload.style.position='absolute';
//    upload.style='margin-left:10px';
    upload.style.margin='2px 0px 0px 20px';
    upload.style.height='71%';
    upload.addEventListener("click" , function(event){
        var fd = new FormData();
        fd.append("file" , blob , filename);
        console.log("userId" ,userId);

        var inputElements = document.getElementsByClassName('v-checkbox');
        var categoriesSelected = [];
            for(var i=0; inputElements[i]; ++i){
                if(inputElements[i].firstElementChild.checked){
                   categoriesSelected.push(inputElements[i].getElementsByTagName('label')[0].textContent);
                }
            }
         if (categoriesSelected.length == 0){
                //var element = document.createElement('div').innerHTML = '<div class="v-label v-widget v-label-undef-w">Please select at least one category</div>';

                alert('Bitte wählen Sie mindestens eine Kategorie aus');
                //alert("please select atleast one category");
                return;

         }
        upload.disabled=true;
        $.ajax({
                type: 'POST',
                url: serverPath+"api/uploadFile?userId="+userId+"&categoriesSelected="+categoriesSelected,
                data: fd,
                enctype: 'multipart/form-data',
                cache: false,
                contentType: false,
                processData: false,
                crossDomain: true,
                success: function (data) {
                      //alert("Uploaded audio file successfully. please close the popup");
                      alert("Hochladen der Audio-Datei erfolgreich ...");
                      document.getElementsByClassName('v-window-closebox')[0].click();
                      console.log("SUCCESS : ", data);
                },
                error: function (e) {
                      console.log("ERROR : ", e);
                      alert(e.responseText);
                }
        });
    })
    li.appendChild(upload);
	//add the li element to the ordered list
	recordingsList.appendChild(li);
}

function __log(e, data) {
	log.innerHTML += "\n" + e + " " + (data || '');
}