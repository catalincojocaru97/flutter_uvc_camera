part of flutter_uvc_camera;

/// 自定义参数 可空  Custom parameters can be empty
class UVCCameraViewParamsEntity {
  /**
   *  if give custom minFps or maxFps or unsupported preview size
   *  set preview possible will fail
   *  **/
  /// camera preview min fps  10
  final int? minFps;

  /// camera preview max fps  60
  final int? maxFps;

  /// camera preview frame format 1 (MJPEG) or 0 (YUV)
  /// DEFAULT 1(MJPEG)  If preview fails and the screen goes black, please try switching to 0
  final int? frameFormat;

  ///  DEFAULT_BANDWIDTH = 1
  final double? bandwidthFactor;

  /// Whether to enable raw preview data callback (NV21) for capture
  /// Defaults to false to reduce overhead unless needed
  final bool? rawPreviewData;

  /// Whether to enable raw image capture support
  /// Defaults to false
  final bool? captureRawImage;

  /// Preferred preview width (e.g., 1920 for Full HD)
  /// If not supported, nearest available size will be used
  final int? previewWidth;

  /// Preferred preview height (e.g., 1080 for Full HD)
  /// If not supported, nearest available size will be used
  final int? previewHeight;

  const UVCCameraViewParamsEntity({
    this.minFps = 10,
    this.maxFps = 60,
    this.bandwidthFactor = 1.0,
    this.frameFormat = 1,
    this.rawPreviewData = false,
    this.captureRawImage = false,
    this.previewWidth = 1920,
    this.previewHeight = 1080,
  });

  Map<String, dynamic> toMap() {
    return {
      "minFps": minFps,
      "maxFps": maxFps,
      "frameFormat": frameFormat,
      "bandwidthFactor": bandwidthFactor,
      "rawPreviewData": rawPreviewData,
      "captureRawImage": captureRawImage,
      "previewWidth": previewWidth,
      "previewHeight": previewHeight,
    };
  }
}
