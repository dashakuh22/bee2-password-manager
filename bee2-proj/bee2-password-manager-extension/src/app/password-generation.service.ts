import {Injectable} from '@angular/core';
import {PasswordSettings} from "./app.component";

@Injectable({
  providedIn: 'root'
})
export class PasswordGenerationService {
  constructor() {
  }

  private alphabetUpper = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"];

  private alphabetLower = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "e", "s", "t", "u", "v", "w", "x", "y", "z"];

  private digits = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];

  private special = ["~", "!", "?", "$", "%", "#", "^", "_", "_", "&", "(", ")", "+", "="];

  public password(passSettings: PasswordSettings, bytes: Uint8Array): string {
    let cLetters = "";

    if (bytes.length == 0) {
      return "";
    }

    if (passSettings.useSymbols) {
      cLetters = this.alphabetLower.join('') + this.alphabetUpper.join('');
    }
    if (passSettings.useDigits) {
      cLetters = cLetters + this.digits.join('');
    }
    if (passSettings.useSpecialChars) {
      cLetters = cLetters + this.special.join('');
    }

    let bytePerSymbol = Math.floor(bytes.length / passSettings.len);
    let cLettersArray = cLetters.split("");

    if (cLettersArray.length == 0) {
      return "";
    }

    for (let iter = 0; iter < 10; iter++) {
      for (let i = 0; i < bytes.length; i += 2) {
        let x = (bytes[i] + iter) % cLettersArray.length;
        let y = (bytes[i + 1] + iter) % cLettersArray.length;
        let swp = cLettersArray[x];
        cLettersArray[x] = cLettersArray[y];
        cLettersArray[y] = swp;
      }
    }

    let password = "";

    let cIndex = 0;
    for (let i = 0; i < passSettings.len; i++) {
      let cSum = 0;
      for (let j = cIndex; j < Math.min(cIndex + bytePerSymbol, bytes.length); j++) {
        cSum ^= bytes[j];
      }
      let cLetterIndx = cSum % cLettersArray.length;
      password += cLettersArray[cLetterIndx];
      cIndex += bytePerSymbol
    }

    return password;
  }

}
