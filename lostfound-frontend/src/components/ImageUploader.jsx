import { useRef, useState } from "react";
import { uploadItemImage } from "../services/itemService";
import Icon from "./Icon";

const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/gif", "image/webp"];
let queueIdCounter = 0;

// Uploads start automatically as soon as a file is added (dropped or
// browsed) - the backend has no "delete image" endpoint, so once a file
// finishes uploading it becomes part of the item permanently; only queued
// (not-yet-uploaded) files can be removed here.
export default function ImageUploader({ itemId, onUploaded }) {
  const [queue, setQueue] = useState([]);
  const [isDragActive, setIsDragActive] = useState(false);
  const inputRef = useRef(null);

  function addFiles(fileList) {
    const files = Array.from(fileList).filter((f) => ALLOWED_TYPES.includes(f.type));
    const entries = files.map((file) => ({
      id: ++queueIdCounter,
      file,
      previewUrl: URL.createObjectURL(file),
      progress: 0,
      status: "uploading",
      error: null,
    }));
    setQueue((prev) => [...prev, ...entries]);
    entries.forEach(startUpload);
  }

  function startUpload(entry) {
    uploadItemImage(itemId, entry.file, (progressEvent) => {
      const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
      setQueue((prev) => prev.map((q) => (q.id === entry.id ? { ...q, progress: percent } : q)));
    })
      .then(({ data }) => {
        setQueue((prev) => prev.map((q) => (q.id === entry.id ? { ...q, status: "done" } : q)));
        const newest = data.images[data.images.length - 1];
        onUploaded?.(newest);
      })
      .catch(() => {
        setQueue((prev) =>
          prev.map((q) => (q.id === entry.id ? { ...q, status: "error", error: "Upload failed" } : q))
        );
      });
  }

  function removeFromQueue(id) {
    setQueue((prev) => prev.filter((q) => q.id !== id));
  }

  function handleDrop(e) {
    e.preventDefault();
    setIsDragActive(false);
    addFiles(e.dataTransfer.files);
  }

  return (
    <div className="image-uploader">
      <div
        className={`dropzone ${isDragActive ? "dropzone-active" : ""}`}
        onDragOver={(e) => {
          e.preventDefault();
          setIsDragActive(true);
        }}
        onDragLeave={() => setIsDragActive(false)}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
      >
        <Icon name="upload" size={28} />
        <p>Drag photos here, or click to browse</p>
        <span className="dropzone-hint">JPG, PNG, GIF, or WEBP</span>
        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png,image/gif,image/webp"
          multiple
          hidden
          onChange={(e) => addFiles(e.target.files)}
        />
      </div>

      {queue.length > 0 && (
        <div className="upload-queue">
          {queue.map((entry) => (
            <div key={entry.id} className="upload-queue-item">
              <img src={entry.previewUrl} alt="" className="upload-thumb" />
              <div className="upload-queue-info">
                {entry.status === "uploading" && (
                  <div className="progress-bar">
                    <div className="progress-bar-fill" style={{ width: `${entry.progress}%` }} />
                  </div>
                )}
                {entry.status === "done" && <span className="upload-status-done">Uploaded</span>}
                {entry.status === "error" && (
                  <span className="upload-status-error">{entry.error}</span>
                )}
              </div>
              {entry.status !== "done" && (
                <button
                  type="button"
                  className="upload-remove-btn"
                  onClick={() => removeFromQueue(entry.id)}
                  aria-label="Remove"
                >
                  <Icon name="close" size={16} />
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
