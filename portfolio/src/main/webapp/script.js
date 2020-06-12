// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

let factIndex = 0;

/**
 * Adds a random fact about me to the page.
 */
function addRandomFact() {
  const facts =
    ['I have a scar on the back of my head from when I fell backwards out of a chair when I was in 5th grade.',
     'I backpacked through Portugal with a Catalan Scout Troop.',
     'I have 6 pets, 5 cats and 1 dog.',
     'I forgot I was in the orchestra pit for "South Pacific."',
     'I still have a baby tooth.',
     'I have baked chocololate chip cookies for my entire highschool more than once.'];

  // Pick a random fact.
  const fact = facts[factIndex % 6];

  // Add it to the page.
  const randomFactContainer = document.getElementById('random-fact-container');
  randomFactContainer.innerText = fact;
  
  factIndex++;
}

let favoriteIndex = 0;

function addRandomFavorite() {
  const favorites =
    ['My favorite color is purple.',
     'My favorite icecream flavor is cookie dough.',
     'My favorite musical is "Next to Normal."',
     'My favorite school subject was math.',
     'My favorite song is currently "Nice to Meet Ya" by Niall Horan.'];
  //Pick a random favorite
  const favorite = favorites[favoriteIndex % 5];

  //Add it to the page
  const randomFavoriteContainer = document.getElementById('random-favorite-container');
  randomFavoriteContainer.innerText = favorite;

  favoriteIndex++;
}

function changePageColor() {
  let color = document.getElementById('colorChange').value;
  switch(color){
    case 'blue':
      document.body.style.backgroundColor = "lightblue";
      changeBordersColor("dodgerblue");
      break;
    case 'red':
      document.body.style.backgroundColor = "mistyrose";
      changeBordersColor("lightcoral");
      break;
    case 'green':
      document.body.style.backgroundColor = "mintcream";
      changeBordersColor("darkseagreen");
      break;
    case 'pink':
      document.body.style.backgroundColor = "pink";
      changeBordersColor("violet");
      break;
    case 'yellow':
      document.body.style.backgroundColor = "cornsilk";
      changeBordersColor("khaki");
      break; 
    default:
      document.body.style.backgroundColor = "lavender";
      changeBordersColor("darkorchid");
      break;
    }
}

function changeBordersColor(color){
  const images = document.getElementsByClassName('images');
  for (let i = 0; i < images.length; i++) {
    images[i].style.border = "10px solid " + color;
  }
  const thinObjects = document.getElementsByClassName('thin-border');
  for (let i = 0; i < thinObjects.length; i++) {
    thinObjects[i].style.border = "5px solid " + color;
  }
}

function onIndexLoad() {
  loadComments();
  checkLogin();
  initMap();
}

let page = 1;
let numberOfPages = 0;
let numberOfComments = 0;
let commentsExternal = [];

function loadComments() {
  fetch('/data').then(response => response.json()).then(comments => {
    commentsExternal = comments;
    addPageButtons(comments.length);
    loadPage(1);
  });
}

function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

function deleteComments() {
  let request = new Request('/delete-data', {method:'DELETE'});
  fetch(request).then(unused => {loadComments()});
}

function addPageButtons(max) {
  numberOfComments = max;
  numberOfPages = Math.ceil(numberOfComments / 5);
  const pageNumbers = document.getElementById('pageNumbers');
  pageNumbers.innerHTML = '';
  let buttonText = ['<', '1'];
  for(let i = 2; i <= numberOfPages; i++){
    buttonText[i] = i.toString();
  }
  buttonText[numberOfPages + 1] = '>';
  for(let i = 0; i < buttonText.length; i++){
    pageNumbers.appendChild(createButtonElement(buttonText[i], i));
  }
}

function createButtonElement(text, number) {
  if(text === '<'){
    return makeBackButton();
  }
  if(text === '>'){
    return makeForwardButton();
  }
  const button = document.createElement('input');
  button.type = 'button';
  button.addEventListener('click', function(){
    loadPage(number);
  });
  button.value = text;
  return button;
}

function makeBackButton() {
  const button = document.createElement('input');
  button.type = 'button';
  button.addEventListener('click', function() {
    goBack();
  });
  button.value = '<';
  return button;
}

function makeForwardButton() {
  const button = document.createElement('input');
  button.type = 'button';
  button.addEventListener('click', function() {
    goForward();
  });
  button.value = '>';
  return button;
}

function goBack() {
  if(page === 1){
    return;
  }
  loadPage(page - 1);
}

function goForward() {
  if(page === numberOfPages){
    return;
  }
  loadPage(page + 1);
}

function loadPage(pageNumber) {
  const commentsElement = document.getElementById('comments-section');
  commentsElement.innerHTML = '';
  let startingCommentNumber = (pageNumber - 1) * 5;
  for(let i=startingCommentNumber; i < startingCommentNumber + 5 && i < numberOfComments; i++){
    commentsElement.appendChild(createListElement(commentsExternal[i]));
  }
  page = pageNumber;  
}

function checkLogin() {
  fetch('/login').then(response => response.json()).then(status => {
    if(status.loggedIn){
      document.getElementById('loggedIn').style.display = 'inline';
      document.getElementById('logout-message').innerHTML = 'If you would like to logout, click <a href="' + status.changeLogInStatusURL + '">here</a>.';
    } else {
      document.getElementById('loggedOut').style.display = 'inline';
      document.getElementById('login-message').innerHTML = 'To post or delete comments, please <a href="' + status.changeLogInStatusURL + '">login</a>.';
    }
  });
}

function initMap() {
  let map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 42.442931, lng: -86.245930}, zoom: 6
  });
  let mapWidget = new MapWidget(map);
  mapWidget.addMarkers([
  [{lat: 42.635067, lng: -83.121789}, 'Brooklands Elementary School: K-4'],
  [{lat: 42.593628, lng: -83.252818}, 'Roeper Lower School: 5'],
  [{lat: 42.550339, lng: -83.206519}, 'Roeper Upper School: 6-12'],
  [{lat: 38.648898, lng: -90.310903}, 'Washington University in St. Louis: Undergrad'],
  ]);
}

class MapWidget {
  constructor(map) {
    this.map = map;
    this.markers = [];
    this.infoWindows = [];
  }
  
  addMarkers(markers) {
    markers.forEach(m => this.addMarker(...m));
  }
  
  addMarker(latLng, windowText) {
    let marker = new google.maps.Marker({
      position: latLng,
      map: this.map
    });
    this.markers.push(marker);
    let infoWindow = new google.maps.InfoWindow({content: windowText});
    marker.addListener('click', () => {
      this.closeWindows();
      infoWindow.open(this.map, marker); 
    });
    this.infoWindows.push(infoWindow);
  }
  
  closeWindows() {
    for (let i = 0; i < this.markers.length; i++) {
      this.infoWindows[i].close(this.map, this.markers[i]);
    }
  }
}

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Creates a chart and adds it to the page. */
function drawChart() {
  const table = new google.visualization.DataTable();
  table.addColumn('string', 'Task');
  table.addColumn('number', 'Minutes');
  table.addRows([
    ['Deciding when to practice', 10],
    ['Waiting to get picked up', 25],
    ['Driving to/from where we\'re practicing', 20],
    ['Waiting for everyone to show up', 30],
    ['Socializing', 30],
    ['Setting up gear', 10],
    ['Playing songs that we aren\'t playing at our next gig', 15],
    ['Deciding our set list', 10],
    ['Rehearsing', 90],
    ['Creating harmonies', 10],
    ['Messing up an entrance or skipping a section', 15]
  ]);

  const options = {
    'width': 640,
    'height': 640,
    'legend': 'none'
  };

  const chart = new google.visualization.PieChart(
    document.getElementById('chart-container')
  );
  chart.draw(table, options);
}
