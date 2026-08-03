import React, { useEffect, useRef } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import { decryptBytes, decompressGzip } from '../utils/qrCrypto';

export const QrScan = ({ onDataReceived }: { onDataReceived: (data: string) => void }) => {
  const scannerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!scannerRef.current) return;

    const html5QrCode = new Html5Qrcode(scannerRef.current.id);

    html5QrCode.start(
      { facingMode: "environment" },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      async (decodedText) => {
        try {
          const binaryString = atob(decodedText);
          const bytes = new Uint8Array(binaryString.length);
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
          }

          const decrypted = await decryptBytes(bytes);
          const json = decompressGzip(decrypted);

          await html5QrCode.stop();
          onDataReceived(json);
        } catch (err) {
          console.error(err);
        }
      },
      () => {}
    ).catch(console.error);

    return () => {
      if (html5QrCode.isScanning) {
        html5QrCode.stop().catch(() => {});
      }
    };
  }, [onDataReceived]);

  return (
    <div className="flex flex-col items-center w-full">
      <div
        id="qr-reader"
        ref={scannerRef}
        className="w-full max-w-sm aspect-square rounded-[32px] overflow-hidden shadow-inner border-2 border-primary/20"
      />
    </div>
  );
};