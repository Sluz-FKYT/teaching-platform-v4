export type MaterialVisibility = 'TEACHER' | 'ALL';

export interface MaterialItem {
  id: number;
  title: string;
  category: string;
  description: string;
  fileName: string;
  visibility: MaterialVisibility;
  uploaderUserId: number;
  downloadUrl: string;
}

export interface UpdateMaterialPayload {
  title?: string;
  category?: string;
  description?: string;
  visibility?: MaterialVisibility;
}

export interface UploadMaterialPayload {
  title: string;
  category: string;
  description: string;
  visibility: MaterialVisibility;
  file: File;
}
