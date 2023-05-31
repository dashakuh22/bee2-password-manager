import {Component} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgbdModalConfirm} from "./auth-modal";
import {environment} from './../environments/environment';

interface MasterLoginStatus {
  status: boolean,
  message: string
}

interface MasterKeyLoadStatus {
  status: boolean,
  masterKey: string
}

interface OtherAuth {
  name: string,
  presented: boolean,
  active: boolean
}

interface OtherAuthStatus {
  status: boolean,
  otherAuths: OtherAuth[]
}

interface OtherService {
  name: string,
  active: boolean,
  presented: boolean
}

interface SwitchServiceResult {
  status: boolean,
  needAuth: boolean,
  authLocation: string,
  otherAuthStatus: OtherAuthStatus
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Менеджер паролей BEE2';

  login: string = "user";
  password: string = "user";

  masterKey: string = "";
  otherAuths: OtherAuth[] = [];

  isLoggedIn: boolean = false;
  isMasterKeyLoaded: boolean = false;

  googleDriveModel: OtherService = {
    active: false,
    name: "google",
    presented: false
  };

  localDriveModel: OtherService = {
    active: false,
    name: "local",
    presented: false
  };

  constructor(private http: HttpClient, private modalService: NgbModal) {

  }

  handleLogin() {
    this.http
      .post<MasterLoginStatus>(environment.apiUrl + `/p/rest/login?username=${this.login}&password=${this.password}`, null,
        {withCredentials: true
        }).subscribe(data => {
        this.isLoggedIn = data.status;
        if (data.status) {
          this.loadMasterKey();
        }
      })
  }

  private loadMasterKey() {
    this.http
      .get<MasterKeyLoadStatus>(environment.apiUrl + "/p/rest/getMasterKey",
        {withCredentials: true
        }).subscribe(data => {
        this.isMasterKeyLoaded = data.status;
        if (data.status) {
          this.masterKey = data.masterKey;
          this.loadOtherAuth();
        }
      })
  }

  private loadOtherAuth() {
    this.http
      .get<OtherAuthStatus>(environment.apiUrl + "/p/rest/otherAuth",
        {withCredentials: true
        }).subscribe(data => {
        if (data.status) {
          this.reloadOtherServices(data.otherAuths);
        }
      });
  }

  getDriveModel(name: string): OtherService | null {
    if (name === 'local') {
      return this.localDriveModel;
    } else if (name === 'google') {
      return this.googleDriveModel;
    }
    return null
  }

  localDriveChanged() {
    this.switchActivity("local");
  }

  googleDriveChange() {
    this.switchActivity("google");
  }

  private switchActivity(type: string) {
    this.http.post<SwitchServiceResult>(environment.apiUrl + `/p/rest/switch/${type}`, null,
      {withCredentials: true
      }).subscribe(data => {
        if (!data.status) {
          return;
        }
        if (data.needAuth) {
          this.modalService.open(NgbdModalConfirm).closed.subscribe(t => {
            window.open(data.authLocation, "_blank");
          })
        }
        this.reloadOtherServices(data.otherAuthStatus.otherAuths);
      });
  }

  private reloadOtherServices(otherAuths: OtherAuth[]) {
    this.otherAuths = otherAuths;
    this.otherAuths.forEach(value => {
      let driveModel = this.getDriveModel(value.name);
      if (driveModel) {
        driveModel.active = value.active;
        driveModel.name = value.name;
        driveModel.presented = value.presented;
      }
    })
  }
}



