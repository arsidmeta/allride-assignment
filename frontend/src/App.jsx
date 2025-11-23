import React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { Container, Typography, Box, Paper } from '@mui/material';
import FileUpload from './components/FileUpload';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Box
        sx={{
          minHeight: '100vh',
          backgroundColor: '#f5f5f5',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Box
          component="header"
          sx={{
            backgroundColor: 'primary.main',
            color: 'white',
            py: 3,
            boxShadow: 2,
          }}
        >
          <Container maxWidth="md">
            <Typography variant="h4" component="h1" gutterBottom>
              Bulk User Import System
            </Typography>
            <Typography variant="body1" sx={{ opacity: 0.9 }}>
              Upload a CSV file to import users in bulk
            </Typography>
          </Container>
        </Box>

        <Container maxWidth="md" sx={{ py: 4, flex: 1 }}>
          <Paper elevation={3} sx={{ p: 4 }}>
            <FileUpload />
          </Paper>
        </Container>
      </Box>
    </ThemeProvider>
  );
}

export default App;

