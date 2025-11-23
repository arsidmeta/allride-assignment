import React, { useState, useRef } from 'react';
import axios from 'axios';
import {
  Box,
  Button,
  Typography,
  Alert,
  LinearProgress,
  Paper,
  Stack,
  IconButton,
} from '@mui/material';
import {
  CloudUpload as CloudUploadIcon,
  InsertDriveFile as FileIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';

const FileUpload = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadStatus, setUploadStatus] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = (event) => {
    const file = event.target.files?.[0];
    
    if (!file) {
      return;
    }

    // Validate file type
    if (!file.name.toLowerCase().endsWith('.csv')) {
      setUploadStatus({
        type: 'error',
        message: 'Please select a CSV file (.csv)'
      });
      setSelectedFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      return;
    }

    setSelectedFile(file);
    setUploadStatus(null);
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setUploadStatus({
        type: 'error',
        message: 'Please select a file first'
      });
      return;
    }

    setIsUploading(true);
    setUploadStatus(null);

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await axios.post('/api/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.data.success) {
        setUploadStatus({
          type: 'success',
          message: response.data.message
        });
        setSelectedFile(null);
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
      } else {
        setUploadStatus({
          type: 'error',
          message: response.data.message || 'Upload failed'
        });
      }
    } catch (error) {
      setUploadStatus({
        type: 'error',
        message: error.response?.data?.message || error.message || 'Upload failed. Please try again.'
      });
    } finally {
      setIsUploading(false);
    }
  };

  const handleRemoveFile = () => {
    setSelectedFile(null);
    setUploadStatus(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Upload CSV File
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Select a CSV file containing user data (id, firstName, lastName, email)
      </Typography>

      {/* File Input Area */}
      <Paper
        variant="outlined"
        sx={{
          p: 3,
          mb: 3,
          border: '2px dashed',
          borderColor: selectedFile ? 'primary.main' : 'grey.300',
          backgroundColor: selectedFile ? 'action.hover' : 'background.paper',
          transition: 'all 0.3s ease',
          cursor: isUploading ? 'not-allowed' : 'pointer',
          opacity: isUploading ? 0.6 : 1,
        }}
        onClick={() => {
          if (!isUploading && fileInputRef.current) {
            fileInputRef.current.click();
          }
        }}
      >
        <Stack spacing={2} alignItems="center">
          <CloudUploadIcon sx={{ fontSize: 48, color: 'primary.main' }} />
          <Typography variant="body1">
            {selectedFile ? 'Click to change file' : 'Click to select CSV file'}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Only CSV files are accepted
          </Typography>
          <input
            ref={fileInputRef}
            type="file"
            accept=".csv"
            onChange={handleFileChange}
            disabled={isUploading}
            style={{ display: 'none' }}
          />
        </Stack>
      </Paper>

      {/* Selected File Display */}
      {selectedFile && (
        <Paper
          variant="outlined"
          sx={{
            p: 2,
            mb: 3,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: 'action.hover',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flex: 1 }}>
            <FileIcon color="primary" />
            <Box sx={{ flex: 1 }}>
              <Typography variant="body1" sx={{ fontWeight: 500 }}>
                {selectedFile.name}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {formatFileSize(selectedFile.size)}
              </Typography>
            </Box>
          </Box>
          <IconButton
            onClick={handleRemoveFile}
            disabled={isUploading}
            color="error"
            size="small"
          >
            <DeleteIcon />
          </IconButton>
        </Paper>
      )}

      {/* Upload Button */}
      <Button
        variant="contained"
        size="large"
        fullWidth
        onClick={handleUpload}
        disabled={!selectedFile || isUploading}
        startIcon={<CloudUploadIcon />}
        sx={{ mb: 3 }}
      >
        {isUploading ? 'Uploading...' : 'Upload File'}
      </Button>

      {/* Progress Bar */}
      {isUploading && (
        <LinearProgress sx={{ mb: 3 }} />
      )}

      {/* Status Messages */}
      {uploadStatus && (
        <Alert
          severity={uploadStatus.type === 'success' ? 'success' : 'error'}
          icon={uploadStatus.type === 'success' ? <CheckCircleIcon /> : <ErrorIcon />}
          onClose={() => setUploadStatus(null)}
          sx={{ mb: 2 }}
        >
          {uploadStatus.message}
        </Alert>
      )}
    </Box>
  );
};

export default FileUpload;

