import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Tab4PageRoutingModule } from './tab4-routing.module';
import { Tab4Page } from './tab4.page';

@NgModule({
  imports: [CommonModule, FormsModule, Tab4PageRoutingModule, Tab4Page],
})
export class Tab4PageModule {}
