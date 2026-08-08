import { Injectable, NgZone, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

declare const google: any;

/**
 * OAuth 2.0 Web client ID from the Google Cloud Console. Unlike the client
 * secret, this value is public — it is served to every browser. It must match
 * `google.oauth.client-id` on the backend, and the app's origin must be listed
 * under "Authorised JavaScript origins" for that client.
 */
export const GOOGLE_CLIENT_ID =
  '640453933697-bdlat8eb32hgnv4o4j63biv82kdtgb5e.apps.googleusercontent.com';

@Injectable({
  providedIn: 'root'
})
export class GoogleSdkService {

  private readonly platformId = inject(PLATFORM_ID);
  private readonly zone = inject(NgZone);

  private scriptLoadedPromise: Promise<void> | null = null;

  loadScript(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) {
      // Nothing to load while prerendering on the server.
      return Promise.resolve();
    }

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
      script.onerror = error => {
        // Let a later attempt retry instead of caching the failure forever.
        this.scriptLoadedPromise = null;
        reject(error);
      };
      document.head.appendChild(script);
    });

    return this.scriptLoadedPromise;
  }

  /**
   * Draws Google's own sign-in button into `container` and hands the resulting
   * ID token to `onCredential`. Google requires their rendered button (or One
   * Tap) for this flow, so it replaces our custom-styled button.
   */
  async renderButton(
    container: HTMLElement,
    onCredential: (idToken: string) => void
  ): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    await this.loadScript();

    google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,

      // Google invokes this outside Angular's zone, so nothing would repaint
      // until the next unrelated event without re-entering it here.
      callback: (response: { credential: string }) =>
        this.zone.run(() => onCredential(response.credential))
    });

    google.accounts.id.renderButton(container, {
      type: 'standard',
      theme: 'outline',
      size: 'large',
      text: 'continue_with',
      shape: 'rectangular',
      logo_alignment: 'left',
      // renderButton only honours widths between 200px and 400px.
      width: Math.min(400, Math.max(200, container.clientWidth || 320))
    });
  }
}
