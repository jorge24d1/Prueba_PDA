import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:mobile_app/screens/login_screen.dart';
import 'package:mobile_app/screens/home_screen.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:mobile_app/services/notification_service.dart';
import 'package:firebase_messaging/firebase_messaging.dart';

// Handler para notificaciones en segundo plano (debe ser top-level)
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp();
  print(
    '🌙 Notificación recibida en SEGUNDO PLANO (Background): ${message.messageId}',
  );
  print('   Título: ${message.notification?.title}');
  print('   Cuerpo: ${message.notification?.body}');
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  try {
    await Firebase.initializeApp();
    // Registrar el handler de background
    FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  } catch (e) {
    print('⚠️ Error inicializando Firebase: $e');
  }

  // Verificar sesión persistente
  final prefs = await SharedPreferences.getInstance();
  final userId = prefs.getString('userId');

  // Si hay sesión activa, inicializamos notificaciones de una vez
  if (userId != null) {
    await NotificationService().initNotifications();
  }

  runApp(MyApp(initialRoute: userId != null ? '/home' : '/login'));
}

class MyApp extends StatelessWidget {
  final String initialRoute;

  MyApp({required this.initialRoute});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Concesionario App',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      initialRoute: initialRoute,
      routes: {
        '/login': (context) => LoginScreen(),
        '/home': (context) => HomeScreen(),
      },
    );
  }
}
