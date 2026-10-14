/**
 * Extract a useful, user-facing message from API, validation, and network errors.
 * Dialog components use this helper so failed actions are reported consistently.
 */
export function getDialogErrorMessage(error: unknown, fallback: string): string {
  const value = error as {
    message?: unknown;
    error?: {
      message?: unknown;
      error?: unknown;
      detail?: unknown;
      title?: unknown;
      errors?: unknown;
    };
  } | null;

  const response = value?.error;
  const candidates: unknown[] = [
    response?.message,
    response?.detail,
    response?.error,
    value?.message
  ];

  for (const candidate of candidates) {
    if (typeof candidate === 'string' && candidate.trim()) {
      return candidate.trim();
    }
  }

  if (response?.errors && typeof response.errors === 'object') {
    const validationMessages = Object.values(response.errors as Record<string, unknown>)
      .flatMap(item => Array.isArray(item) ? item : [item])
      .filter((item): item is string => typeof item === 'string' && !!item.trim());

    if (validationMessages.length) {
      return validationMessages.join(' ');
    }
  }

  // Angular HttpErrorResponse uses status 0 when the server cannot be reached.
  if (typeof (error as { status?: unknown } | null)?.status === 'number'
      && (error as { status: number }).status === 0) {
    return 'Unable to reach the server. Check your connection and try again.';
  }

  return fallback;
}
