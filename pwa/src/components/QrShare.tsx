import React, { useEffect, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { compressString, encryptBytes, bytesToBase64 } from '../utils/qrCrypto';

export const QrShare = ({ jsonData }: { jsonData: string }) => {
  const [chunks, setChunks] = useState<string[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    const processData = async () => {
      try {
        const compressed = compressString(jsonData);
        const encrypted = await encryptBytes(compressed);
        const base64Data = bytesToBase64(encrypted);

        const chunkSize = 150;
        const totalChunks = Math.ceil(base64Data.length / chunkSize);
        const frames: string[] = [];

        for (let i = 0; i < totalChunks; i++) {
          const start = i * chunkSize;
          const end = Math.min(start + chunkSize, base64Data.length);
          frames.push(`${i}/${totalChunks}|${base64Data.substring(start, end)}`);
        }

        setChunks(frames);
        setCurrentIndex(0);
      } catch (err) {
        console.error(err);
      }
    };

    processData();
  }, [jsonData]);

  useEffect(() => {
    if (chunks.length <= 1) return;

    const interval = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % chunks.length);
    }, 250);

    return () => clearInterval(interval);
  }, [chunks.length]);

  if (chunks.length === 0) return null;

  return (
    <div className="flex flex-col items-center justify-center p-6 bg-surface rounded-[32px] border border-outline/10 shadow-lg">
      <div className="bg-white p-4 rounded-2xl">
        <QRCodeSVG value={chunks[currentIndex]} size={280} level="L" />
      </div>
      {chunks.length > 1 && (
        <div className="mt-4 flex flex-col items-center gap-2">
          <div className="w-32 h-1.5 bg-surface-variant rounded-full overflow-hidden">
            <div
              className="h-full bg-primary transition-all duration-200"
              style={{ width: `${((currentIndex + 1) / chunks.length) * 100}%` }}
            />
          </div>
          <span className="text-[10px] font-black text-on-surface-variant opacity-60">
            {currentIndex + 1} / {chunks.length}
          </span>
        </div>
      )}
    </div>
  );
};
