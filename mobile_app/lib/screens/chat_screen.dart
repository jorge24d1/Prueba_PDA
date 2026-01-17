import 'package:flutter/material.dart';
import 'package:mobile_app/services/api_service.dart';

class ChatScreen extends StatefulWidget {
  @override
  _ChatScreenState createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final TextEditingController _controller = TextEditingController();
  final ApiService _apiService = ApiService();
  final List<Map<String, dynamic>> _messages = []; // {text, isUser, vehicle?}
  bool _isLoading = false;
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    // Mensaje de bienvenida inicial
    _addMessage(
      "¡Hola! Soy Dante 🤖, tu asesor experto de NextGen Motors. ¿Qué tipo de vehículo estás buscando hoy?",
      false,
    );
  }

  void _addMessage(String text, bool isUser, {Map<String, dynamic>? vehicle}) {
    setState(() {
      _messages.add({"text": text, "isUser": isUser, "vehicle": vehicle});
    });
    // Scroll al fondo
    Future.delayed(Duration(milliseconds: 100), () {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  Future<void> _sendMessage() async {
    final text = _controller.text.trim();
    if (text.isEmpty) return;

    _controller.clear();
    _addMessage(text, true);

    setState(() => _isLoading = true);

    try {
      final response = await _apiService.obtenerRecomendacion(text);
      final botReply = response['textoRespuesta'] ?? "Lo siento, no entendí.";
      final vehicles = response['vehiculosRecomendados'] as List<dynamic>?;

      Map<String, dynamic>? vehicleData;
      if (vehicles != null && vehicles.isNotEmpty) {
        vehicleData = vehicles.first;
      }

      _addMessage(botReply, false, vehicle: vehicleData);
    } catch (e) {
      _addMessage(
        "Lo siento, tuve un problema de conexión. Inténtalo de nuevo.",
        false,
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Chat con Dante AI 🤖')),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: EdgeInsets.all(16),
              itemCount: _messages.length,
              itemBuilder: (context, index) {
                final msg = _messages[index];
                final isUser = msg['isUser'];
                final vehicle = msg['vehicle'];

                return Column(
                  crossAxisAlignment: isUser
                      ? CrossAxisAlignment.end
                      : CrossAxisAlignment.start,
                  children: [
                    Container(
                      margin: EdgeInsets.symmetric(vertical: 4),
                      padding: EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: isUser ? Colors.blue : Colors.grey[200],
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        msg['text'],
                        style: TextStyle(
                          color: isUser ? Colors.white : Colors.black87,
                        ),
                      ),
                    ),
                    if (vehicle != null) _buildVehicleCard(vehicle),
                  ],
                );
              },
            ),
          ),
          if (_isLoading)
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: LinearProgressIndicator(),
            ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _controller,
                    decoration: InputDecoration(
                      hintText: 'Escribe tu mensaje...',
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(24),
                      ),
                      contentPadding: EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 12,
                      ),
                    ),
                    onSubmitted: (_) => _sendMessage(),
                  ),
                ),
                SizedBox(width: 8),
                IconButton(
                  icon: Icon(Icons.send, color: Colors.blue),
                  onPressed: _sendMessage,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildVehicleCard(Map<String, dynamic> v) {
    return Container(
      margin: EdgeInsets.only(top: 8, bottom: 8, right: 40),
      child: Card(
        elevation: 3,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                "${v['marca']} ${v['modelo']}",
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
              SizedBox(height: 4),
              Text("📅 Año: ${v['año']}"),
              Text("💰 Precio: \$${v['precio']}"),
              Text("⛽ Combustible: ${v['combustible']}"),
              SizedBox(height: 8),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () {
                    // Aquí podrías navegar al detalle o agendar cita
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text("¡Interesante! Agenda una cita.")),
                    );
                  },
                  child: Text("Ver Detalles"),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
