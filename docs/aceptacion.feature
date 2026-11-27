# language: es
Característica: Consulta móvil de comprobantes públicos
  Escenario: Acceso
    Dado un enlace vigente
    Cuando lo abro
    Entonces puedo consultar productos, cantidades, total y forma de pago sin iniciar sesión

  Escenario: Privacidad
    Dado un comprobante fiado
    Cuando lo consulto
    Entonces no se muestran datos del cliente ni costos de compra

  Escenario: Anulación
    Dado un comprobante de venta anulada
    Cuando lo consulto
    Entonces aparece «Anulada»
    Y un token inválido ofrece error genérico
