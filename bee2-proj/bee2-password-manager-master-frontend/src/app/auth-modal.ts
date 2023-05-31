import { Component, Type } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'ngbd-modal-confirm',
  standalone: true,
  template: `
		<div class="modal-header">
			<h4 class="modal-title" id="modal-title">Необходима авторизация</h4>
			<button
				type="button"
				class="btn-close"
				aria-describedby="modal-title"
				(click)="modal.dismiss('Cross click')"
			></button>
		</div>
		<div class="modal-body">
			<p>
				<strong>Для включения сохранения в <strong class="text-primary">Google Drive</strong> необходима авторизация.</strong>
			</p>
			<p>
				При нажатии на кнопку <strong>OK</strong> вы будете перенаправлены на страницу авторизации!
			</p>
		</div>
		<div class="modal-footer">
			<button type="button" class="btn btn-outline-secondary" (click)="modal.dismiss('cancel click')">Отмена</button>
			<button type="button" class="btn btn-danger" (click)="modal.close('Ok click')">ОК</button>
		</div>
	`,
})
export class NgbdModalConfirm {
  constructor(public modal: NgbActiveModal) {

  }
}
