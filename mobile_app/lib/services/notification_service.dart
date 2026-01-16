import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:mobile_app/services/api_service.dart';

class NotificationService {
  final FirebaseMessaging _firebaseMessaging = FirebaseMessaging.instance;
  final ApiService _apiService = ApiService();

  Future<void> initNotifications() async {
    // 1. Pedir permiso
    NotificationSettings settings = await _firebaseMessaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );

    if (settings.authorizationStatus == AuthorizationStatus.authorized) {
      print('🔔 Permiso de notificaciones concedido');

      // 2. Obtener Token
      final token = await _firebaseMessaging.getToken();
      if (token != null) {
        print('🔑 FCM Token: $token');
        // Enviar al backend
        await _apiService.sendFcmToken(token);
      }

      // 3. Escuchar en primer plano
      FirebaseMessaging.onMessage.listen((RemoteMessage message) {
        print('📩 Notificación recibida en primer plano:');
        print('   Título: ${message.notification?.title}');
        print('   Cuerpo: ${message.notification?.body}');
        // Aquí podrías mostrar un dialog o snackbar
      });
    } else {
      print('🔕 Permiso de notificaciones denegado');
    }
  }
}
