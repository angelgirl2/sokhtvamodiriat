const express = require('express');
const path = require('path');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// API endpoint for vehicle & fuel card status
app.get('/api/status', (req, res) => {
    res.json({
        app: "سامانه مدیریت خودرو و کارت سوخت",
        status: "Online & Active",
        version: "1.0.0",
        features: [
            "شبیه‌ساز هوشمند کارت سوخت",
            "استعلام خلافی آنلاین با پلاک",
            "مدیریت یادآورهای سرویس دوره‌ای",
            "نمایش استاندارد پلاک ملی ایران"
        ],
        timestamp: new Date().toISOString()
    });
});

// HTML Landing Dashboard
app.get('/', (req, res) => {
    res.send(`
    <!DOCTYPE html>
    <html lang="fa" dir="rtl">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>سامانه مدیریت خودرو و کارت سوخت</title>
        <style>
            :root {
                --bg-color: #0f172a;
                --card-bg: #1e293b;
                --accent-color: #38bdf8;
                --text-color: #f8fafc;
                --text-secondary: #94a3b8;
                --success-color: #10b981;
            }
            body {
                font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                background-color: var(--bg-color);
                color: var(--text-color);
                margin: 0;
                padding: 20px;
                display: flex;
                flex-direction: column;
                align-items: center;
                min-height: 100vh;
                justify-content: center;
            }
            .container {
                max-width: 700px;
                width: 100%;
                background-color: var(--card-bg);
                border-radius: 20px;
                padding: 30px;
                box-shadow: 0 10px 25px rgba(0,0,0,0.5);
                border: 1px solid #334155;
            }
            h1 {
                color: var(--accent-color);
                font-size: 26px;
                margin-bottom: 10px;
                text-align: center;
            }
            p.subtitle {
                text-align: center;
                color: var(--text-secondary);
                margin-bottom: 30px;
            }
            .badge {
                background-color: rgba(16, 185, 129, 0.15);
                color: var(--success-color);
                padding: 6px 12px;
                border-radius: 20px;
                font-size: 13px;
                font-weight: bold;
                display: inline-block;
                margin-bottom: 20px;
            }
            .features-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 15px;
                margin-bottom: 30px;
            }
            .feature-card {
                background-color: #0f172a;
                padding: 15px;
                border-radius: 12px;
                border: 1px solid #334155;
            }
            .feature-card h3 {
                margin: 0 0 8px 0;
                font-size: 15px;
                color: var(--accent-color);
            }
            .feature-card p {
                margin: 0;
                font-size: 13px;
                color: var(--text-secondary);
            }
            .footer {
                text-align: center;
                color: var(--text-secondary);
                font-size: 12px;
                border-top: 1px solid #334155;
                padding-top: 15px;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div style="text-align: center;">
                <span class="badge">● سرویس روی بستر ریلیوی (Railway) فعال است</span>
            </div>
            <h1>سامانه مدیریت خودرو و کارت سوخت</h1>
            <p class="subtitle">نسخه ابری و وب‌سرویس پایش هوشمند خودرو، کارت سوخت و استعلام خلافی</p>
            
            <div class="features-grid">
                <div class="feature-card">
                    <h3>💳 کارت سوخت هوشمند</h3>
                    <p>مدیریت و شبیه‌ساز کارت هوشمند سوخت با قابلیت چرخش سه‌بعدی و نمایش اطلاعات کامل.</p>
                </div>
                <div class="feature-card">
                    <h3>⚡ استعلام خلافی آنلاین</h3>
                    <p>سامانه استعلام خلافی و عوارض با شماره پلاک استاندارد ملی و اتصال به ربات بله.</p>
                </div>
                <div class="feature-card">
                    <h3>🔔 یادآورهای سرویس</h3>
                    <p>پایش کارکرد خودرو و هشدار تعویض روغن، تسمه تایم و قطعات مصرفی.</p>
                </div>
                <div class="feature-card">
                    <h3>🛡️ بیمه‌نامه هوشمند</h3>
                    <p>محاسبه روزهای باقی‌مانده تا سررسید بیمه شخص ثالث و بدنه.</p>
                </div>
            </div>

            <div class="footer">
                قدرت گرفته از Node.js و استقرار خودکار روی Railway • متصل به گیت‌هاب
            </div>
        </div>
    </body>
    </html>
    `);
});

app.listen(PORT, () => {
    console.log(\`Server is running on port \${PORT}\`);
});
