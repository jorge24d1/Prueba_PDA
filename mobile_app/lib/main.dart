import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:mobile_app/screens/login_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // IMPORTANTE: Esto requiere que google-services.json esté en android/app/
  try {
    await Firebase.initializeApp();
  } catch (e) {
    print('⚠️ Error inicializando Firebase (¿Falta google-services.json?): $e');
  }

  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Concesionario App',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: LoginScreen(),
    );
  }
}
