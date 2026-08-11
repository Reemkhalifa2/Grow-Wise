import { Pipe, PipeTransform } from '@angular/core';

/**
 * Converts a raw backend enum string (MUTUAL_FUND, HIGH, ACTIVE) into a
 * human-readable label (Mutual Fund, High, Active). Backend enums are
 * never shown to the user verbatim.
 */
@Pipe({
  name: 'enumLabel',
  standalone: true
})
export class EnumLabelPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    return value
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}
