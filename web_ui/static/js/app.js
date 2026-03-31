// 初始化Lucide图标
document.addEventListener('DOMContentLoaded', () => {
    lucide.createIcons();
});

// 全局状态
let currentMode = 'single';
let chatHistory = [];
let currentBatchItems = [];
let isGenerating = false;

// 自动调整输入框高度
function autoResize(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px';
}

// 处理键盘事件
function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
}

// 填充输入框
function fillInput(text) {
    const input = document.getElementById('userInput');
    input.value = text;
    autoResize(input);
    input.focus();
}

// 清空输入框
function clearInput() {
    const input = document.getElementById('userInput');
    input.value = '';
    input.style.height = 'auto';
}

// 发送消息
async function sendMessage() {
    const input = document.getElementById('userInput');
    const message = input.value.trim();

    if (!message || isGenerating) return;

    addMessage('user', message);
    clearInput();
    showLoading(true);
    isGenerating = true;

    try {
        const response = await fetch('/api/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                requirement: message,
                session_id: generateSessionId()
            })
        });

        const data = await response.json();

        if (data.success) {
            addMessage('assistant', data.result);
        } else {
            addMessage('assistant', '抱歉，生成过程中出现错误：' + data.error);
        }
    } catch (error) {
        console.error('Error:', error);
        addMessage('assistant', '网络错误，请稍后重试。');
    } finally {
        showLoading(false);
        isGenerating = false;
    }
}

// 添加消息到聊天区域
function addMessage(role, content) {
    const messagesArea = document.getElementById('messagesArea');

    if (messagesArea.querySelector('.welcome-message')) {
        messagesArea.innerHTML = '';
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${role}`;

    const avatar = role === 'user' ? 'user' : 'bot';
    const author = role === 'user' ? '你' : 'AI助手';
    const time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });

    let formattedContent = formatContent(content);

    messageDiv.innerHTML = `
        <div class="message-avatar">
            <i data-lucide="${avatar}"></i>
        </div>
        <div class="message-content">
            <div class="message-header">
                <span class="message-author">${author}</span>
                <span class="message-time">${time}</span>
            </div>
            <div class="message-text">${formattedContent}</div>
        </div>
    `;

    messagesArea.appendChild(messageDiv);
    lucide.createIcons();
    messagesArea.scrollTop = messagesArea.scrollHeight;

    messageDiv.querySelectorAll('pre code').forEach((block) => {
        hljs.highlightElement(block);
    });
}

// 格式化内容（处理代码块）
function formatContent(content) {
    if (typeof content !== 'string') {
        content = JSON.stringify(content, null, 2);
    }

    content = content.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
        const language = lang || 'text';
        return `<div class="code-block">
            <div class="code-block-header">
                <span class="code-language">${language}</span>
                <div class="code-actions">
                    <button class="code-action-btn" onclick="copyToClipboard(this)" title="复制">
                        <i data-lucide="copy" style="width: 14px; height: 14px;"></i>
                    </button>
                </div>
            </div>
            <pre><code class="language-${language}">${escapeHtml(code.trim())}</code></pre>
        </div>`;
    });

    content = content.replace(/`([^`]+)`/g, '<code style="background: var(--bg-tertiary); padding: 2px 6px; border-radius: 4px; font-family: monospace;">$1</code>');
    content = content.replace(/\n/g, '<br>');

    return content;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

async function copyToClipboard(btn) {
    const codeBlock = btn.closest('.code-block').querySelector('code');
    const text = codeBlock.textContent;

    try {
        await navigator.clipboard.writeText(text);
        const originalHTML = btn.innerHTML;
        btn.innerHTML = '<i data-lucide="check" style="width: 14px; height: 14px; color: var(--success-color);"></i>';
        lucide.createIcons();

        setTimeout(() => {
            btn.innerHTML = originalHTML;
            lucide.createIcons();
        }, 2000);
    } catch (err) {
        console.error('复制失败:', err);
    }
}

function showLoading(show) {
    const loading = document.getElementById('loadingOverlay');
    if (show) {
        loading.classList.remove('hidden');
    } else {
        loading.classList.add('hidden');
    }
}

function switchMode(mode) {
    currentMode = mode;

    document.querySelectorAll('.mode-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.mode === mode) {
            btn.classList.add('active');
        }
    });

    if (mode === 'single') {
        document.getElementById('singleMode').classList.remove('hidden');
        document.getElementById('batchMode').classList.add('hidden');
    } else {
        document.getElementById('singleMode').classList.add('hidden');
        document.getElementById('batchMode').classList.remove('hidden');
    }
}

function handleFileUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const content = JSON.parse(e.target.result);
            if (Array.isArray(content)) {
                currentBatchItems = content;
                showBatchPreview();
            } else {
                alert('文件格式错误：需要JSON数组');
            }
        } catch (err) {
            alert('文件解析错误：' + err.message);
        }
    };
    reader.readAsText(file);
}

function showBatchPreview() {
    document.getElementById('uploadArea').classList.add('hidden');
    document.getElementById('batchPreview').classList.remove('hidden');
    document.getElementById('itemCount').textContent = currentBatchItems.length;

    const list = document.getElementById('previewList');
    list.innerHTML = currentBatchItems.map((item, index) => `
        <div class="preview-item">
            <div class="preview-number">${index + 1}</div>
            <div class="preview-content">
                <div class="preview-title">${item.name || '未命名需求'}</div>
                <div class="preview-desc">${item.description || ''}</div>
            </div>
            <div class="preview-type">${item.type || '标准扩展'}</div>
        </div>
    `).join('');
}

function clearBatch() {
    currentBatchItems = [];
    document.getElementById('uploadArea').classList.remove('hidden');
    document.getElementById('batchPreview').classList.add('hidden');
    document.getElementById('batchProgress').classList.add('hidden');
    document.getElementById('fileInput').value = '';
}

async function startBatchProcess() {
    document.getElementById('batchPreview').classList.add('hidden');
    document.getElementById('batchProgress').classList.remove('hidden');

    const progressList = document.getElementById('progressList');
    const progressFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');

    progressList.innerHTML = currentBatchItems.map((item, index) => `
        <div class="progress-item" id="progress-${index}">
            <div class="progress-status pending">
                <i data-lucide="clock" style="width: 12px; height: 12px;"></i>
            </div>
            <div class="preview-content">
                <div class="preview-title">${item.name || '未命名需求'}</div>
            </div>
        </div>
    `).join('');
    lucide.createIcons();

    for (let i = 0; i < currentBatchItems.length; i++) {
        const item = currentBatchItems[i];
        const progressItem = document.getElementById(`progress-${i}`);

        progressItem.querySelector('.progress-status').className = 'progress-status processing';
        progressItem.querySelector('.progress-status').innerHTML = '<i data-lucide="loader-2" style="width: 12px; height: 12px;"></i>';
        lucide.createIcons();

        try {
            const response = await fetch('/api/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    requirement: item.description,
                    session_id: generateSessionId()
                })
            });

            const data = await response.json();

            if (data.success) {
                progressItem.querySelector('.progress-status').className = 'progress-status completed';
                progressItem.querySelector('.progress-status').innerHTML = '<i data-lucide="check" style="width: 12px; height: 12px;"></i>';
            } else {
                throw new Error(data.error);
            }
        } catch (error) {
            progressItem.querySelector('.progress-status').className = 'progress-status error';
            progressItem.querySelector('.progress-status').innerHTML = '<i data-lucide="x" style="width: 12px; height: 12px;"></i>';
        }

        lucide.createIcons();

        const progress = ((i + 1) / currentBatchItems.length) * 100;
        progressFill.style.width = progress + '%';
        progressText.textContent = `${i + 1}/${currentBatchItems.length}`;

        await new Promise(resolve => setTimeout(resolve, 500));
    }

    setTimeout(() => {
        alert('批量生成完成！');
    }, 500);
}

function generateSessionId() {
    return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

function startNewChat() {
    document.getElementById('messagesArea').innerHTML = `
        <div class="welcome-message">
            <div class="welcome-icon">
                <i data-lucide="sparkles" class="sparkle-icon"></i>
            </div>
            <h2>金蝶星瀚HR 定制化开发代码助手</h2>
            <p>输入您的开发需求，我将为您生成高质量的定制化代码</p>
            <div class="quick-actions">
                <button class="quick-btn" onclick="fillInput('开发一个考勤打卡插件，支持GPS位置校验')">
                    <i data-lucide="map-pin"></i>
                    考勤打卡插件
                </button>
                <button class="quick-btn" onclick="fillInput('生成月度考勤汇总报表SQL')">
                    <i data-lucide="bar-chart-2"></i>
                    考勤报表
                </button>
                <button class="quick-btn" onclick="fillInput('开发薪酬计算规则扩展插件')">
                    <i data-lucide="calculator"></i>
                    薪酬计算
                </button>
                <button class="quick-btn" onclick="fillInput('创建员工信息查询REST API')">
                    <i data-lucide="search"></i>
                    查询API
                </button>
            </div>
        </div>
    `;
    lucide.createIcons();
}

function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('open');
}

function showCodeModal(title, code, language) {
    document.getElementById('modalTitle').textContent = title;
    const codeBlock = document.getElementById('codeBlock');
    codeBlock.className = `language-${language}`;
    codeBlock.textContent = code;
    hljs.highlightElement(codeBlock);
    document.getElementById('codeModal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('codeModal').classList.add('hidden');
}

function copyCode() {
    const code = document.getElementById('codeBlock').textContent;
    navigator.clipboard.writeText(code).then(() => {
        alert('代码已复制到剪贴板');
    });
}

function downloadCode() {
    const code = document.getElementById('codeBlock').textContent;
    const title = document.getElementById('modalTitle').textContent;
    const blob = new Blob([code], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = title + '.txt';
    a.click();
    URL.revokeObjectURL(url);
}

document.addEventListener('DOMContentLoaded', () => {
    const uploadArea = document.getElementById('uploadArea');
    if (uploadArea) {
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.querySelector('.upload-box').style.borderColor = 'var(--primary-color)';
        });

        uploadArea.addEventListener('dragleave', () => {
            uploadArea.querySelector('.upload-box').style.borderColor = '';
        });

        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.querySelector('.upload-box').style.borderColor = '';

            const files = e.dataTransfer.files;
            if (files.length > 0) {
                document.getElementById('fileInput').files = files;
                handleFileUpload({ target: { files: files } });
            }
        });
    }
});
