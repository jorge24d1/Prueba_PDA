const https = require('https');
const fs = require('fs');
const crypto = require('crypto');

// 1. CONFIGURACIÓN (Rellena esto)
const PROJECT_ID = 'nextgen-c9f08';
const DEVICE_TOKEN = process.argv[2]; // Pasaremos el token por consola

if (!DEVICE_TOKEN) {
    console.error("❌ Error: Debes pasar el Device Token como argumento.");
    console.error("Ejemplo: node debug_fcm.js fPNp-I4QQk...");
    process.exit(1);
}

// Cargar Service Account (asegúrate de que el archivo existe)
// Usamos el archivo que ya teníamos en el proyecto (aunque esté ignorado en git, debería estar en disco)
const SERVICE_ACCOUNT_PATH = 'src/main/resources/nextgen-c9f08-firebase-adminsdk-fbsvc-1cf71ae7de.json';

if (!fs.existsSync(SERVICE_ACCOUNT_PATH)) {
    console.error("❌ Error: No encuentro el archivo JSON de credenciales en: " + SERVICE_ACCOUNT_PATH);
    process.exit(1);
}

const serviceAccount = JSON.parse(fs.readFileSync(SERVICE_ACCOUNT_PATH));

// 2. GENERAR JWT TOKEN (Manual, sin librerías externas para evitar npm install)
function getJwt() {
    const header = { alg: 'RS256', typ: 'JWT' };
    const now = Math.floor(Date.now() / 1000);
    const claim = {
        iss: serviceAccount.client_email,
        scope: 'https://www.googleapis.com/auth/firebase.messaging',
        aud: 'https://oauth2.googleapis.com/token',
        exp: now + 3600,
        iat: now
    };

    const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64').replace(/=/g, '');
    const encodedClaim = Buffer.from(JSON.stringify(claim)).toString('base64').replace(/=/g, '');

    const sign = crypto.createSign('RSA-SHA256');
    sign.update(encodedHeader + '.' + encodedClaim);
    const signature = sign.sign(serviceAccount.private_key, 'base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');

    return `${encodedHeader}.${encodedClaim}.${signature}`;
}

// 3. OBTENER ACCESS TOKEN DE GOOGLE
function getAccessToken() {
    return new Promise((resolve, reject) => {
        const jwt = getJwt();
        const postData = `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`;

        const req = https.request({
            hostname: 'oauth2.googleapis.com',
            path: '/token',
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        }, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                const json = JSON.parse(data);
                if (json.access_token) resolve(json.access_token);
                else reject(json);
            });
        });

        req.on('error', reject);
        req.write(postData);
        req.end();
    });
}

// 4. ENVIAR NOTIFICACIÓN (FCM V1 API)
async function sendNotification() {
    try {
        console.log("🔄 Obteniendo Access Token...");
        const accessToken = await getAccessToken();
        console.log("✅ Token obtenido.");

        const payload = {
            message: {
                token: DEVICE_TOKEN,
                notification: {
                    title: "Prueba Directa Script",
                    body: "Si ves esto, Firebase y tu Token funcionan. El problema es Azure."
                },
                data: {
                    click_action: "FLUTTER_NOTIFICATION_CLICK"
                }
            }
        };

        console.log("🔄 Enviando mensaje a Firebase...");
        const req = https.request({
            hostname: 'fcm.googleapis.com',
            path: `/v1/projects/${PROJECT_ID}/messages:send`,
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        }, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                console.log(`\n📬 Respuesta Firebase (Status ${res.statusCode}):`);
                console.log(data);
            });
        });

        req.write(JSON.stringify(payload));
        req.end();

    } catch (error) {
        console.error("❌ Error:", error);
    }
}

sendNotification();
