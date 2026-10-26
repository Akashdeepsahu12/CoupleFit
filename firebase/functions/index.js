const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Layer 3 Real-Time Sync: Cloud Function for Critical Events
 * Triggers when a new critical alert (e.g. High HR, AFib) is added to the user's sharedView.
 * Instantly sends an FCM push notification to the partner.
 */
exports.onCriticalAlertCreated = functions.firestore
    .document('users/{userId}/sharedView/alerts/{alertId}')
    .onCreate(async (snap, context) => {
        const newAlert = snap.data();
        const userId = context.params.userId;

        // Fetch the user's profile to find their linked partner
        const userRef = admin.firestore().collection('users').doc(userId);
        const userDoc = await userRef.get();
        const partnerId = userDoc.data().partnerId;

        if (!partnerId) {
            console.log('No partner linked, aborting notification.');
            return null;
        }

        // Fetch the partner's FCM token
        const partnerRef = admin.firestore().collection('users').doc(partnerId);
        const partnerDoc = await partnerRef.get();
        const fcmToken = partnerDoc.data().fcmToken;

        if (!fcmToken) {
            console.log('Partner has no FCM token.');
            return null;
        }

        // Send FCM notification
        const payload = {
            notification: {
                title: 'Critical Health Alert',
                body: `Your partner recorded a critical alert: ${newAlert.type}`
            },
            data: {
                alertId: context.params.alertId,
                type: newAlert.type,
                timestamp: String(newAlert.timestamp)
            }
        };

        return admin.messaging().sendToDevice(fcmToken, payload);
    });

