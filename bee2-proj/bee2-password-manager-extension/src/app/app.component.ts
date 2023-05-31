import {Component} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from './../environments/environment';
import {PasswordGenerationService} from "./password-generation.service";
import {CardSelectedOption} from "./file-card/file-card.component";

declare var Module: any;

interface ActivationResult {
  status: boolean,
  login: string,
  message: string
}

export interface PasswordSettings {
  useSymbols: boolean,
  useDigits: boolean,
  useSpecialChars: boolean,
  len: number
}

interface FileInterface {
  data: Uint8Array,
  saveOption: string,
  idx: number
}

interface LoadFileInfo {
  filename: string | null,
  index: number,
  data: string,
  type: string
}

interface LoadFilesResult {
  passwordsSeedKeys: string[],
  files: LoadFileInfo[]
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'bee2-password-manager-extension';

  password: string = "";
  secretKey: string = "";
  isActivated: boolean = false;
  userLogin: string = "login";
  errorMessage: string | null = "";
  bytes: Uint8Array = new Uint8Array();
  passwordSettings: PasswordSettings = {
    useSymbols: true,
    useDigits: true,
    useSpecialChars: true,
    len: 16
  };
  generatedPassword: string = "";
  files: FileInterface[] = [];
  optionsToSave: string[] = [];
  recentFiles: LoadFilesResult = {
    files: [],
    passwordsSeedKeys: []
  };
  fileLoaded: boolean = false;

  constructor(private http: HttpClient, private passwordGenerationService: PasswordGenerationService) {
    this.resetPlugin();
  }

  public generatePasswordSeed() {
    Module.cwrap("PwdGenShare", null, null, null)();
    this.bytes = Module.FS.readFile('gen_password');
    this.files = [
      {
        data: this.readFS_File("abc"),
        idx: 0,
        saveOption: this.optionsToSave[0]
      },
      {
        data: this.readFS_File("cde"),
        idx: 1,
        saveOption: this.optionsToSave[0]
      }
    ]
    this.password = new TextDecoder().decode(this.bytes);
  }

  public generatePassword() {
    this.generatedPassword = this.passwordGenerationService.password(this.passwordSettings, this.bytes);
  }

  public clearPassword() {
    this.password = "";
    this.generatedPassword = "";
    this.bytes = new Uint8Array();
  }

  public activatePlugin() {
    this.http.post<ActivationResult>(environment.apiUrl + `/master-key/${this.secretKey}`, null)
      .subscribe(data => {
        if (data.status) {
          this.isActivated = true;
          this.userLogin = data.login;
        }
        this.errorMessage = data.message;
        this.loadRecentFiles();
      })
  }

  public resetPlugin() {
    this.password = "";
    this.secretKey = "f0b8c03c-e291-11ed-b5ea-0242ac120002";
    this.isActivated = false;
    this.userLogin = "login";
    this.errorMessage = "";
    this.bytes = new Uint8Array();
    this.passwordSettings = {
      useSymbols: true,
      useDigits: true,
      useSpecialChars: true,
      len: 16
    };
    this.generatedPassword = "";
    this.files = [];
    this.optionsToSave = [
      "google",
      "local"
    ]
    this.recentFiles = {
      passwordsSeedKeys: [],
      files: []
    };
    this.fileLoaded = false;
  }

  onCardChange($event: CardSelectedOption) {
    this.files[$event.idx].saveOption = $event.option;
    console.log(this.files);
  }

  saveClickProcess() {
    this.files.forEach(value => {
      const filename = this.password.substring(0, 4) + "-password-" + (value.idx + 1) + ".pass";
      if (value.saveOption == 'google') {
        this.saveGoogle(filename, value);
      }
      if (value.saveOption == 'local') {
        this.saveLocal(filename, value);
      }
    });
    this.http.post<ActivationResult>(environment.apiUrl + `/save-info/${this.secretKey}`, {
      files: [
        this.files[0].saveOption,
        this.files[1].saveOption
      ],
      key: this.password.substring(0, 4)
    }).subscribe();
    this.loadRecentFiles();
  }

  private saveLocal(filename: string, value: FileInterface) {
    const blob: Blob = new Blob([value.data], {type: 'application/octet-stream'});
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    link.click();
    link.remove();
    this.http.post(environment.apiUrl + `/save/google/${this.secretKey}`, {
      filename: filename,
      index: value.idx,
      data: null,
      type: value.saveOption
    }).subscribe();
  }

  private saveGoogle(filename: string, value: FileInterface) {
    console.log("Save", filename, value);
    this.http.post(environment.apiUrl + `/save/google/${this.secretKey}`, {
      filename: filename,
      index: value.idx,
      data: Array.from(value.data),
      type: value.saveOption
    }).subscribe();
  }

  public loadRecentFiles() {
    this.http.get<LoadFilesResult>(environment.apiUrl + `/load/${this.secretKey}`)
      .subscribe(data => {
        this.recentFiles = data;
      })
  }

  loadRecentFileGoogle(file: LoadFileInfo) {
    let filename = file.index == 0 ? "abc" : "cde";
    let uint8Array = this.base64ToArrayBuffer(file.data);
    console.log(filename, uint8Array);
    this.writeFS_File(filename, uint8Array);
    this.fileLoaded = true;
  }

  loadRecentFileLocal(file: LoadFileInfo, event: any) {
    let filename = file.index == 0 ? "abc" : "cde";
    const selectedFile: File = event.target.files[0];
    selectedFile.arrayBuffer().then(value => {
      let uint8Array1 = new Uint8Array(value);
      console.log(filename, uint8Array1);
      this.writeFS_File(filename, uint8Array1);
      this.fileLoaded = true;
    });
  }

  private arrayBufferToBase64(buffer: ArrayBuffer) {
    let binary = '';
    let bytes = new Uint8Array(buffer);
    let len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }

  private base64ToArrayBuffer(base64: string) {
    let binaryString = atob(base64);
    let bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes;
  }

  private readFS_File(filename: string) {
    let stream = Module.FS.open(filename, 'r');
    let buf = new Uint8Array(160);
    Module.FS.read(stream, buf, 0, buf.length, 0);
    Module.FS.close(stream);
    console.log(buf)
    return buf;
  }

  private writeFS_File(filename: string, data: Uint8Array) {
    let stream = Module.FS.open(filename, 'w+');
    Module.FS.write(stream, data, 0, data.length, 0);
    Module.FS.close(stream);
  }

  loadNewSeed() {
    Module.cwrap("PwdReadShare", null, null, null)();
    this.bytes = Module.FS.readFile('read_password');
    this.files = [
      {
        data: this.readFS_File("abc"),
        idx: 0,
        saveOption: this.optionsToSave[0]
      },
      {
        data: this.readFS_File("cde"),
        idx: 1,
        saveOption: this.optionsToSave[0]
      }
    ]
    this.password = new TextDecoder().decode(this.bytes);
    this.generatePassword();
  }
}
