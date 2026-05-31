import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, FormsModule, TranslateModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'CRM Finanse';
  menuOpen = false;
  currentLang = 'pl';

  availableLanguages = [
    { code: 'pl', name: 'Polski' },
    { code: 'en', name: 'English' },
    { code: 'de', name: 'Deutsch' },
    { code: 'fr', name: 'Francais' },
    { code: 'es', name: 'Espanol' },
    { code: 'it', name: 'Italiano' },
    { code: 'pt', name: 'Portugues' },
    { code: 'nl', name: 'Nederlands' },
    { code: 'ru', name: 'Russkij' },
    { code: 'uk', name: 'Ukrainska' },
    { code: 'cs', name: 'Cestina' },
    { code: 'sk', name: 'Slovencina' },
    { code: 'sv', name: 'Svenska' },
    { code: 'no', name: 'Norsk' },
    { code: 'da', name: 'Dansk' }
  ];

  constructor(private translate: TranslateService) {
    const savedLang = localStorage.getItem('language') || 'pl';
    this.currentLang = savedLang;
    this.translate.setDefaultLang('pl');
    this.translate.use(savedLang);
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  changeLanguage(lang: string): void {
    this.currentLang = lang;
    this.translate.use(lang);
    localStorage.setItem('language', lang);
  }
}
