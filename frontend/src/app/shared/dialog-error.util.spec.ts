import { describe, expect, it } from 'vitest';
import { getDialogErrorMessage } from './dialog-error.util';

describe('getDialogErrorMessage', () => {
  it('uses the server message when the API returns a structured error', () => {
    expect(getDialogErrorMessage(
      { error: { message: 'Department code already exists.' } },
      'Save failed.'
    )).toBe('Department code already exists.');
  });

  it('supports a string response body', () => {
    expect(getDialogErrorMessage(
      { error: 'The requested record conflicts with an existing record.' },
      'Save failed.'
    )).toBe('The requested record conflicts with an existing record.');
  });

  it('combines validation messages', () => {
    expect(getDialogErrorMessage(
      { error: { errors: { name: ['Name is required.'], code: ['Code is invalid.'] } } },
      'Save failed.'
    )).toBe('Name is required. Code is invalid.');
  });

  it('explains network failures', () => {
    expect(getDialogErrorMessage({ status: 0 }, 'Save failed.'))
      .toBe('Unable to reach the server. Check your connection and try again.');
  });

  it('uses the supplied fallback when no meaningful message is available', () => {
    expect(getDialogErrorMessage({}, 'Save failed.')).toBe('Save failed.');
  });
});
