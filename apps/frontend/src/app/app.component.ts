import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RulesEditorComponent } from './components/rules-editor/rules-editor.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RulesEditorComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Rules Editor';
}
