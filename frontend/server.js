const express = require('express');
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = 3000;

// Enable CORS for API calls
app.use(cors());

// Serve static files from current directory
app.use(express.static(path.join(__dirname)));

// Route for the main page
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(PORT, () => {
    console.log(`Frontend server running at http://localhost:${PORT}`);
    console.log('Make sure your backend services are running on:');
    console.log('- API Gateway: http://localhost:8080');
    console.log('- User Service: http://localhost:8081');
    console.log('- Document Service: http://localhost:8082');
    console.log('- Version Control: http://localhost:8083');
});
