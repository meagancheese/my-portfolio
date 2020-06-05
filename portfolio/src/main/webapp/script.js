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
  console.log(color);
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
  for(var i = 0; i < document.images.length; i++){
    document.images.item(i).style.border = "10px solid " + color;
  }
  document.getElementById('comments').style.border = "5px solid " + color;
}

function loadComments() {
  console.log('loadComments starts');
  let max = document.getElementById('maxButton').value;
  addPageButtons(max);
  fetch('/data?max=' + max).then(response => response.json()).then(comments => {
    // console.log(comments); DEBUG Tool
    const commentsElement = document.getElementById('comments-section');
    commentsElement.innerHTML = '';
    for(let i=0; i < comments.length; i++){
      commentsElement.appendChild(createListElement(comments[i]));
    }
  });
}

function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

function deleteComments() {
  let request = new Request('/delete-data', {method:'DELETE'});
  fetch(request).then(unused => {/*console.log('Delete finishes'); DEBUG Tool*/loadComments()});
}

function addPageButtons(max) {
  
}