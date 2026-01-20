import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:mobile_app/services/api_service.dart';

class NotificationService {
  final FirebaseMessaging _firebaseMessaging = FirebaseMessaging.instance;
  final FlutterLocalNotificationsPlugin _localNotifications =
      FlutterLocalNotificationsPlugin();
  final ApiService _apiService = ApiService();

  Future<void> initNotifications() async {
    // 0. Configurar Notificaciones Locales
    const AndroidInitializationSettings initializationSettingsAndroid =
        AndroidInitializationSettings('@mipmap/ic_launcher');

    // Configuración para iOS (opcional, pero recomendada)
    const DarwinInitializationSettings initializationSettingsDarwin =
        DarwinInitializationSettings();

    const InitializationSettings initializationSettings =
        InitializationSettings(
          android: initializationSettingsAndroid,
          iOS: initializationSettingsDarwin,
        );

    print('🚀 Iniciando NotificationService...');

    await _localNotifications.initialize(initializationSettings);
    print('✅ Notificaciones locales inicializadas');

    // 0.1 Crear Canal de Notificaciones (Importante para segundo plano en Android)
    const AndroidNotificationChannel channel = AndroidNotificationChannel(
      'high_importance_channel', // id
      'Notificaciones Importantes', // title
      description:
          'Este canal se usa para notificaciones importantes.', // description
      importance: Importance.max,
    );

    final AndroidFlutterLocalNotificationsPlugin? androidImplementation =
        _localNotifications
            .resolvePlatformSpecificImplementation<
              AndroidFlutterLocalNotificationsPlugin
            >();

    if (androidImplementation != null) {
      await androidImplementation.createNotificationChannel(channel);
      print('✅ Canal de notificaciones Android creado: ${channel.id}');
    } else {
      print('ℹ️ No se pudo obtener implementación Android (¿estás en iOS?)');
    }

    // 1. Pedir permiso (Firebase)
    print('⏳ Solicitando permisos de notificación...');
    NotificationSettings settings = await _firebaseMessaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );

    print('📣 Estado de autorización: ${settings.authorizationStatus}');

    if (settings.authorizationStatus == AuthorizationStatus.authorized) {
      print('🔔 Permiso de notificaciones concedido');

      // 2. Obtener Token
      try {
        final token = await _firebaseMessaging.getToken();
        if (token != null) {
          print('🔑 FCM Token obtenido: $token');
          // Enviar al backend
          await _apiService.sendFcmToken(token);
        } else {
          print('⚠️ FCM Token es nulo');
        }
      } catch (e) {
        print('❌ Error obteniendo FCM Token: $e');
      }

      // 3. Escuchar en primer plano
      FirebaseMessaging.onMessage.listen((RemoteMessage message) {
        print('📩 Notificación recibida en PRIMER PLANO (Foreground):');
        print('   Título: ${message.notification?.title}');
        print('   Cuerpo: ${message.notification?.body}');
        print('   Data: ${message.data}');

        // Mostrar notificación local
        RemoteNotification? notification = message.notification;
        AndroidNotification? android = message.notification?.android;

        if (notification != null && android != null) {
          _localNotifications.show(
            notification.hashCode,
            notification.title,
            notification.body,
            const NotificationDetails(
              android: AndroidNotificationDetails(
                'high_importance_channel', // id del canal
                'Notificaciones Importantes', // nombre del canal
                importance: Importance.max,
                priority: Priority.high,
                icon: '@mipmap/ic_launcher',
              ),
              iOS: DarwinNotificationDetails(),
            ),
          );
        }
      });
    } else {
      print('🔕 Permiso de notificaciones denegado');
    }
  }
}
