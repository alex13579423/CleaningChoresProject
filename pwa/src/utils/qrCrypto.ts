import { gzip, ungzip } from 'pako';

export const compressString = (data: string): Uint8Array => {
  // Use TextEncoder to safely convert the string to bytes before compressing
  const encoder = new TextEncoder();
  return gzip(encoder.encode(data));
};

export const decompressGzip = (data: Uint8Array): string => {
  // Use TextDecoder instead of the deprecated { to: 'string' } option
  const decompressed = ungzip(data);
  const decoder = new TextDecoder();
  return decoder.decode(decompressed);
};

const getCryptoKey = async (): Promise<CryptoKey> => {
  const encoder = new TextEncoder();
  const keyString = "cleaning_chores_default_secret_key";
  const hashBuffer = await crypto.subtle.digest("SHA-256", encoder.encode(keyString));
  const keyBytes = hashBuffer.slice(0, 16);

  return await crypto.subtle.importKey(
    "raw",
    keyBytes,
    { name: "AES-GCM" },
    false,
    ["encrypt", "decrypt"]
  );
};

export const encryptBytes = async (plaintext: Uint8Array): Promise<Uint8Array> => {
  const key = await getCryptoKey();
  const iv = crypto.getRandomValues(new Uint8Array(12));

  // Cast plaintext to unknown then BufferSource to satisfy strict TS checks
  const ciphertext = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv: iv, tagLength: 128 },
    key,
    plaintext as unknown as BufferSource
  );

  const result = new Uint8Array(12 + ciphertext.byteLength);
  result.set(iv, 0);
  result.set(new Uint8Array(ciphertext), 12);

  return result;
};

export const decryptBytes = async (combined: Uint8Array): Promise<Uint8Array> => {
  const key = await getCryptoKey();
  const iv = combined.slice(0, 12);
  const ciphertext = combined.slice(12);

  // Cast ciphertext to unknown then BufferSource here as well
  const decrypted = await crypto.subtle.decrypt(
    { name: "AES-GCM", iv: iv, tagLength: 128 },
    key,
    ciphertext as unknown as BufferSource
  );

  return new Uint8Array(decrypted);
};

// Robust binary-safe Base64 conversion for cross-platform compatibility
export const bytesToBase64 = (bytes: Uint8Array): string => {
  let binString = '';
  bytes.forEach((byte) => {
    binString += String.fromCharCode(byte);
  });
  return btoa(binString);
};

export const base64ToBytes = (base64: string): Uint8Array => {
  const binString = atob(base64);
  const bytes = new Uint8Array(binString.length);
  for (let i = 0; i < binString.length; i++) {
    bytes[i] = binString.charCodeAt(i);
  }
  return bytes;
};