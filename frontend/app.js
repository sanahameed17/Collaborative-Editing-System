// Global variables
let currentUser = null;
let currentToken = null;
let currentDocument = null;
let currentView = 'documents'; // 'documents' or 'templates'
let websocket = null;

// API Base URL
const API_BASE = 'http://localhost:8080';

// DOM Elements
const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const userInfo = document.getElementById('user-info');
const usernameDisplay = document.getElementById('username-display');
const statusMessage = document.getElementById('status-message');

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
    setupWebSocket();
});

// Authentication Functions
function checkAuthStatus() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');

    if (token && user) {
        currentToken = token;
        currentUser = JSON.parse(user);
        showApp();
        loadDocuments();
    } else {
        showAuth();
    }
}

function showAuth() {
    authSection.classList.remove('hidden');
    appSection.classList.add('hidden');
    userInfo.classList.add('hidden');
}

function showApp() {
    authSection.classList.add('hidden');
    appSection.classList.remove('hidden');
    userInfo.classList.remove('hidden');
    usernameDisplay.textContent = `Welcome, ${currentUser.username}!`;
}

function showLogin() {
    document.getElementById('login-tab').classList.add('active');
    document.getElementById('register-tab').classList.remove('active');
    document.getElementById('login-form').classList.remove('hidden');
    document.getElementById('register-form').classList.add('hidden');
}

function showRegister() {
    document.getElementById('register-tab').classList.add('active');
    document.getElementById('login-tab').classList.remove('active');
    document.getElementById('register-form').classList.remove('hidden');
    document.getElementById('login-form').classList.add('hidden');
}

async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        });

        if (response.ok) {
            const data = await response.json();
            currentToken = data.token;
            currentUser = { username };

            localStorage.setItem('token', currentToken);
            localStorage.setItem('user', JSON.stringify(currentUser));

            showMessage('Login successful!', 'success');
            showApp();
            loadDocuments();
        } else {
            showMessage('Login failed. Please check your credentials.', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();

    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const firstName = document.getElementById('reg-firstname').value;
    const lastName = document.getElementById('reg-lastname').value;
    const password = document.getElementById('reg-password').value;

    try {
        const response = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username,
                password,
                email,
                firstName,
                lastName,
            }),
        });

        if (response.ok) {
            showMessage('Registration successful! Please login.', 'success');
            showLogin();
        } else {
            showMessage('Registration failed. Please try again.', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    currentToken = null;
    currentUser = null;
    currentDocument = null;
    showAuth();
    showMessage('Logged out successfully.', 'info');
}

// Document Functions
async function loadDocuments() {
    try {
        const response = await fetch(`${API_BASE}/api/documents`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            const documents = await response.json();
            displayDocuments(documents);
        } else {
            showMessage('Failed to load documents.', 'error');
        }
    } catch (error) {
        showMessage('Network error while loading documents.', 'error');
    }
}

function displayDocuments(documents) {
    console.log('Displaying documents:', documents.length);
    const documentsList = document.getElementById('documents-list');
    documentsList.innerHTML = '';

    if (documents.length === 0) {
        documentsList.innerHTML = `
            <div class="empty-state">
                <h3>No documents yet</h3>
                <p>Create your first document or use a template to get started!</p>
            </div>
        `;
        return;
    }

    documents.forEach(doc => {
        console.log('Creating document item for:', doc.id, doc.title);
        const docElement = document.createElement('div');
        docElement.className = 'document-item';
        docElement.setAttribute('data-doc-id', doc.id);
        docElement.onclick = () => {
            console.log('Document clicked:', doc.id);
            loadDocument(doc.id);
        };

        const isOwner = doc.owner === currentUser.username;
        // For the sidebar, we'll show owner for owned documents and assume read-write for shared ones
        // The actual permission will be determined when the document is loaded
        const permission = isOwner ? 'owner' : 'read-write';

        docElement.innerHTML = `
            <h4>${doc.title}</h4>
            <p>${doc.content ? doc.content.substring(0, 50) + '...' : 'Empty document'}</p>
            <div class="document-meta">
                <span>Owner: ${doc.owner}</span>
                <span class="permission-badge ${permission.replace('_', '-')}">${permission.replace('_', ' ').toUpperCase()}</span>
            </div>
            <div class="document-actions">
                ${isOwner ? `<button onclick="event.stopPropagation(); deleteDocument(${doc.id})" class="btn btn-small" style="background: #e74c3c;">🗑️</button>` : ''}
            </div>
        `;

        documentsList.appendChild(docElement);
    });
}

async function loadDocument(id) {
    console.log('Loading document with ID:', id);
    console.log('Current token exists:', !!currentToken);
    console.log('Current user exists:', !!currentUser);

    try {
        const response = await fetch(`${API_BASE}/api/documents/${id}`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        console.log('Load document response status:', response.status);

        if (response.ok) {
            const documentData = await response.json();
            console.log('Document data received:', documentData);

            // Fetch user permission for this document
            const permissionResponse = await fetch(`${API_BASE}/api/documents/${id}/permission`, {
                headers: {
                    'Authorization': `Bearer ${currentToken}`,
                },
            });

            let userPermission = 'read-only'; // default
            if (permissionResponse.ok) {
                userPermission = await permissionResponse.text();
                userPermission = userPermission.replace(/"/g, '').trim(); // Remove quotes and trim whitespace
                // Convert backend enum format to frontend format
                if (userPermission === 'READ_WRITE' || userPermission === 'WRITE') {
                    userPermission = 'read-write';
                } else if (userPermission === 'READ_ONLY') {
                    userPermission = 'read-only';
                }
            }

            // Add permission to document data
            documentData.userPermission = userPermission;
            currentDocument = documentData;
            displayDocument(currentDocument);

            // Update active document in sidebar
            document.querySelectorAll('.document-item').forEach(item => {
                item.classList.remove('active');
            });
            // Find the document item that was clicked and mark it as active
            const activeElement = document.querySelector(`.document-item[data-doc-id="${id}"]`);
            if (activeElement) {
                activeElement.classList.add('active');
                console.log('Marked document as active');
            } else {
                console.log('Could not find active element for document ID:', id);
            }
        } else {
            const errorText = await response.text();
            console.error('Failed to load document. Response:', errorText);
            showMessage('Failed to load document.', 'error');
        }
    } catch (error) {
        console.error('Network error while loading document:', error);
        showMessage('Network error while loading document.', 'error');
    }
}

function displayDocument(doc) {
    console.log('Displaying document:', doc.id, doc.title, 'Content length:', doc.content ? doc.content.length : 0);

    // Clear the editor first
    const editor = document.getElementById('document-editor');
    editor.value = '';

    // Update document title
    document.getElementById('document-title').textContent = doc.title;

    // Update document content
    editor.value = doc.content || '';
    console.log('Editor content set to:', editor.value);

    // Update document metadata
    const isOwner = doc.owner === currentUser.username;
    const permission = isOwner ? 'owner' : (doc.userPermission || 'read-only');

    document.getElementById('document-owner').textContent = `Owner: ${doc.owner}`;
    const permissionBadge = document.getElementById('document-permission');
    permissionBadge.textContent = permission.replace('_', ' ').toUpperCase();
    permissionBadge.className = `permission-badge ${permission.replace('_', '-')}`;

    // Enable/disable editor based on permissions
    const saveBtn = document.querySelector('.document-actions .btn-primary');

    // Allow editing if user is owner or has read-write permission
    const canEdit = isOwner ||
                   permission === 'read-write' ||
                   permission === 'READ_WRITE' ||
                   permission === 'read_write' ||
                   permission === 'WRITE' ||
                   permission.toLowerCase() === 'write';

    if (!canEdit) {
        editor.disabled = true;
        editor.style.backgroundColor = '#f5f5f5';
        saveBtn.disabled = true;
        saveBtn.style.opacity = '0.5';
    } else {
        editor.disabled = false;
        editor.style.backgroundColor = '';
        saveBtn.disabled = false;
        saveBtn.style.opacity = '';
    }

    console.log('Document display completed');
}

async function createDocument(event) {
    event.preventDefault();

    const title = document.getElementById('doc-title').value;
    const content = document.getElementById('doc-content').value;

    try {
        const response = await fetch(`${API_BASE}/api/documents`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`,
            },
            body: JSON.stringify({ title, content }),
        });

        if (response.ok) {
            const newDoc = await response.json();
            showMessage('Document created successfully!', 'success');
            hideCreateModal();
            loadDocuments();
            loadDocument(newDoc.id);
        } else {
            showMessage('Failed to create document.', 'error');
        }
    } catch (error) {
        showMessage('Network error while creating document.', 'error');
    }
}

async function saveDocument() {
    if (!currentDocument) {
        showMessage('No document selected.', 'error');
        return;
    }

    const content = document.getElementById('document-editor').value;

    try {
        const response = await fetch(`${API_BASE}/api/documents/${currentDocument.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`,
            },
            body: JSON.stringify({ content }),
        });

        if (response.ok) {
            showMessage('Document saved successfully!', 'success');
            currentDocument.content = content;
        } else {
            showMessage('Failed to save document.', 'error');
        }
    } catch (error) {
        showMessage('Network error while saving document.', 'error');
    }
}

async function deleteDocument(id) {
    if (!confirm('Are you sure you want to delete this document?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/documents/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            showMessage('Document deleted successfully!', 'success');
            loadDocuments();
            if (currentDocument && currentDocument.id === id) {
                currentDocument = null;
                document.getElementById('document-title').textContent = 'Select a document to start editing';
                document.getElementById('document-editor').value = '';
            }
        } else {
            showMessage('Failed to delete document.', 'error');
        }
    } catch (error) {
        showMessage('Network error while deleting document.', 'error');
    }
}

// Template Functions
function showTemplates() {
    currentView = 'templates';
    document.getElementById('document-view').classList.add('hidden');
    document.getElementById('templates-view').classList.remove('hidden');
    loadTemplates();
}

function showDocumentView() {
    currentView = 'documents';
    document.getElementById('templates-view').classList.add('hidden');
    document.getElementById('document-view').classList.remove('hidden');
}

async function loadTemplates() {
    try {
        const response = await fetch(`${API_BASE}/api/documents/templates`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            const templates = await response.json();
            displayTemplates(templates);
        } else {
            showMessage('Failed to load templates.', 'error');
        }
    } catch (error) {
        showMessage('Network error while loading templates.', 'error');
    }
}

function displayTemplates(templates) {
    const templatesGrid = document.getElementById('templates-grid');
    templatesGrid.innerHTML = '';

    if (templates.length === 0) {
        templatesGrid.innerHTML = `
            <div class="empty-state">
                <h3>No templates available</h3>
                <p>Templates will help you get started quickly with pre-formatted documents.</p>
            </div>
        `;
        return;
    }

    templates.forEach(template => {
        const templateElement = document.createElement('div');
        templateElement.className = 'template-card';

        templateElement.innerHTML = `
            <h4>${template.name}</h4>
            <p>${template.description}</p>
            <span class="template-category">${template.category}</span>
            <div class="template-actions">
                <button onclick="useTemplate(${template.id})" class="btn btn-primary">Use Template</button>
                <button onclick="previewTemplate(${template.id})" class="btn btn-secondary">Preview</button>
            </div>
        `;

        templatesGrid.appendChild(templateElement);
    });
}

async function useTemplate(templateId) {
    const title = prompt('Enter document title:');
    if (!title) return;

    try {
        const response = await fetch(`${API_BASE}/api/documents/templates/${templateId}/create-document`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`,
            },
            body: JSON.stringify({ title }),
        });

        if (response.ok) {
            const newDoc = await response.json();
            showMessage('Document created from template!', 'success');
            showDocumentView();
            loadDocuments();
            loadDocument(newDoc.id);
        } else {
            showMessage('Failed to create document from template.', 'error');
        }
    } catch (error) {
        showMessage('Network error while creating document.', 'error');
    }
}

async function previewTemplate(templateId) {
    try {
        const response = await fetch(`${API_BASE}/api/documents/templates/${templateId}`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            const template = await response.json();
            // Show preview modal
            alert(`Template: ${template.name}\n\nDescription: ${template.description}\n\nContent Preview:\n${template.content.substring(0, 200)}...`);
        } else {
            showMessage('Failed to load template preview.', 'error');
        }
    } catch (error) {
        showMessage('Network error while loading template.', 'error');
    }
}

// Sharing Functions
function showShareModal() {
    if (!currentDocument) {
        showMessage('No document selected.', 'error');
        return;
    }

    document.getElementById('share-doc-title').textContent = currentDocument.title;
    document.getElementById('export-doc-title').textContent = currentDocument.title;
    document.getElementById('share-modal').classList.remove('hidden');
    loadDocumentShares();
}

function hideShareModal() {
    document.getElementById('share-modal').classList.add('hidden');
}

async function shareDocument(event) {
    event.preventDefault();

    const username = document.getElementById('share-username').value;
    const permission = document.getElementById('share-permission').value;

    try {
        const response = await fetch(`${API_BASE}/api/documents/${currentDocument.id}/share`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`,
            },
            body: JSON.stringify({
                sharedWithUser: username,
                permission: permission
            }),
        });

        if (response.ok) {
            showMessage('Document shared successfully!', 'success');
            document.getElementById('share-username').value = '';
            loadDocumentShares();
            loadDocuments(); // Refresh to show shared documents
        } else {
            showMessage('Failed to share document.', 'error');
        }
    } catch (error) {
        showMessage('Network error while sharing document.', 'error');
    }
}

async function loadDocumentShares() {
    try {
        const response = await fetch(`${API_BASE}/api/documents/${currentDocument.id}/shares`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            const shares = await response.json();
            displayShares(shares);
        } else {
            showMessage('Failed to load shares.', 'error');
        }
    } catch (error) {
        showMessage('Network error while loading shares.', 'error');
    }
}

function displayShares(shares) {
    const sharesList = document.getElementById('shares-list');
    sharesList.innerHTML = '';

    if (shares.length === 0) {
        sharesList.innerHTML = '<p style="text-align: center; color: #666; padding: 1rem;">No shares yet</p>';
        return;
    }

    shares.forEach(share => {
        const shareElement = document.createElement('div');
        shareElement.className = 'share-item';

        shareElement.innerHTML = `
            <div class="share-details">
                <div class="share-user">${share.sharedWithUser}</div>
                <div class="share-permission">${share.permission.replace('_', ' ')}</div>
            </div>
            <button onclick="revokeShare('${share.sharedWithUser}')" class="revoke-btn">Revoke</button>
        `;

        sharesList.appendChild(shareElement);
    });
}

async function revokeShare(username) {
    if (!confirm(`Are you sure you want to revoke access for ${username}?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/documents/${currentDocument.id}/share/${username}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            showMessage('Share revoked successfully!', 'success');
            loadDocumentShares();
            loadDocuments();
        } else {
            showMessage('Failed to revoke share.', 'error');
        }
    } catch (error) {
        showMessage('Network error while revoking share.', 'error');
    }
}

// Export Functions
function showExportModal() {
    if (!currentDocument) {
        showMessage('No document selected.', 'error');
        return;
    }

    document.getElementById('export-modal').classList.remove('hidden');
}

function hideExportModal() {
    document.getElementById('export-modal').classList.add('hidden');
}

async function exportDocument(format) {
    if (!currentDocument) {
        showMessage('No document selected.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/documents/${currentDocument.id}/export/${format}`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `${currentDocument.title}.${format}`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            showMessage(`Document exported as ${format.toUpperCase()}!`, 'success');
            hideExportModal();
        } else {
            showMessage('Failed to export document.', 'error');
        }
    } catch (error) {
        showMessage('Network error while exporting document.', 'error');
    }
}

// Modal Functions
function showCreateDocument() {
    document.getElementById('create-modal').classList.remove('hidden');
    document.getElementById('doc-title').focus();
}

function hideCreateModal() {
    document.getElementById('create-modal').classList.add('hidden');
    document.getElementById('doc-title').value = '';
    document.getElementById('doc-content').value = '';
}

async function showVersionHistory() {
    if (!currentDocument) {
        showMessage('No document selected.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/versions/${currentDocument.id}/history`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`,
            },
        });

        if (response.ok) {
            const versions = await response.json();
            displayVersionHistory(versions);
            document.getElementById('history-modal').classList.remove('hidden');
        } else {
            showMessage('Failed to load version history.', 'error');
        }
    } catch (error) {
        showMessage('Network error while loading version history.', 'error');
    }
}

function displayVersionHistory(versions) {
    const versionList = document.getElementById('version-list');
    versionList.innerHTML = '';

    if (versions.length === 0) {
        versionList.innerHTML = '<p>No version history available.</p>';
        return;
    }

    versions.forEach(version => {
        const versionElement = document.createElement('div');
        versionElement.className = 'version-item';

        const timestamp = new Date(version.timestamp).toLocaleString();

        versionElement.innerHTML = `
            <h4>Version ${version.id}</h4>
            <p>Edited by: ${version.editedBy}</p>
            <small>${timestamp}</small>
        `;

        versionList.appendChild(versionElement);
    });
}

function hideHistoryModal() {
    document.getElementById('history-modal').classList.add('hidden');
}

// WebSocket Functions
function setupWebSocket() {
    // WebSocket connection for real-time collaboration
    // This would connect to the document editing service
    console.log('WebSocket setup ready for real-time collaboration');
}

// Utility Functions
function showMessage(message, type) {
    const statusDiv = document.getElementById('status-message');
    statusDiv.textContent = message;
    statusDiv.className = `status-message ${type}`;
    statusDiv.classList.remove('hidden');

    setTimeout(() => {
        statusDiv.classList.add('hidden');
    }, 3000);
}

// Auto-save functionality (optional)
let autoSaveTimer;
document.getElementById('document-editor').addEventListener('input', function() {
    clearTimeout(autoSaveTimer);
    autoSaveTimer = setTimeout(() => {
        if (currentDocument) {
            saveDocument();
        }
    }, 2000); // Auto-save after 2 seconds of inactivity
});
