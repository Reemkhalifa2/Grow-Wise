// google-sdk.service.ts
import { Injectable } from '@angular/core';

declare const google: any;

@Injectable({
  providedIn: 'root'
})
export class GoogleSdkService {
  private scriptLoadedPromise: Promise<void> | null = null;

  loadScript(): Promise<void> {
    if (typeof google !== 'undefined' && google?.accounts) {
      return Promise.resolve();
    }

    if (this.scriptLoadedPromise) {
      return this.scriptLoadedPromise;
    }

    this.scriptLoadedPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = (err) => reject(err);
      document.head.appendChild(script);
    });

    return this.scriptLoadedPromise;
  }
}