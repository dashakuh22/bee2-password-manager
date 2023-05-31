import {Component, EventEmitter, Input, Output} from '@angular/core';


export interface CardSelectedOption {
  idx: number,
  option: string
}

@Component({
  selector: 'app-file-card',
  templateUrl: './file-card.component.html',
  styleUrls: ['./file-card.component.css']
})
export class FileCardComponent {

  @Input()
  fileData: Uint8Array = new Uint8Array();

  @Input()
  prefix: string = "";

  @Input()
  idx: number = 0;

  @Input()
  options: string[] = [];

  @Output()
  onChange: EventEmitter<CardSelectedOption> = new EventEmitter();
  onChangeOption(value: any) {
    this.onChange.emit({
      idx: this.idx,
      option: value.target.value
    });
  }
}
