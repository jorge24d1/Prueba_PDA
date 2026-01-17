import 'package:flutter/material.dart';
import 'package:mobile_app/services/api_service.dart';
import 'package:intl/intl.dart';
import 'package:mobile_app/screens/chat_screen.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _apiService = ApiService();
  List<dynamic> _citas = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _cargarCitas();
  }

  Future<void> _cargarCitas() async {
    try {
      final citas = await _apiService.getCitas();
      setState(() {
        _citas = citas;
        _isLoading = false;
      });
    } catch (e) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Error cargando citas')));
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Mis Citas')),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _cargarCitas,
              child: ListView.builder(
                itemCount: _citas.length,
                itemBuilder: (context, index) {
                  final cita = _citas[index];
                  return Card(
                    margin: EdgeInsets.all(8.0),
                    child: ListTile(
                      leading: Icon(Icons.calendar_today, color: Colors.blue),
                      title: Text(cita['vehiculo'] ?? 'Cita General'),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Estado: ${cita['estado']}'),
                          Text(
                            'Fecha: ${cita['fechaAsignada'] ?? "Pendiente asignación"}',
                          ),
                        ],
                      ),
                      trailing: Icon(Icons.arrow_forward_ios, size: 16),
                    ),
                  );
                },
              ),
            ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => ChatScreen()),
          );
        },
        label: Text('Chat con Dante'),
        icon: Icon(Icons.chat),
        backgroundColor: Colors.blueAccent,
      ),
    );
  }
}
