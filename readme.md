# WebServer
La classe `WebServer` encapsule les fonctionnalités de `HTTPServer` et fournit un routeur pour la création des routes d'une API ainsi qu'un module pour la gestion des **Server Sent Events** (SSE).

### Création et démarrage
Pour créer un serveur web au sein de votre application, il suffit d'instancier la classe `WebServer`, puis d'appeler sa méthode `listen` en précisant le port d'écoute du serveur :

``` java
//Instanciaition de WebServer
WebServer webServer = new WebServer();

//Démarrage du serveur qui écoutera le port 8080
webServer.listen(8080);
```

### Création d'une route
``WebServer`` dispose d'un attribut de type ``WebServerRouter`` dont le rôle est de gérer les routes de votre API.

La création d'une route se fait en deux temps :
1. La création d'un contrôleur qui se chargera de traiter la requpete entrante
2. L'ajout d'une route qui associera un chemin (path) et la méthode du contrôleur à appeler. 

#### Contrôleur
Les méthodes du contrôleur qui seront appelées par le routeur doivent être ``static`` et prendre en paramètre une instance de ``WebServerContext`` :

``` java
public class UsersController
{
    public static void findAll(WebServerContext context)
    {
        // Todo
    }

    public static void create(WebServerContext context)
    {
        // Todo
    }
}
```

``WebServerContext`` offre un accès à trois attribut :
- ``request`` de type ``WebServerRequest`` qui contient les informations relatives à la requête transmise par le client.
- ``response`` de type ``WebServerResponse`` qui contient les méthodes permettant de répondre au client.
- ``sse`` de type ``WebServerSSE`` qui contient les méthodes permettant d'émettre un Server Sent Event vers les clients connectés au canal fourni.

### Route
Une fois la méthode du contrôleur créée, il est possible de l'associer à une route de votre API :

``` java
webServer.getRouter().get("/users", UsersController::findAll);
webServer.getRouter().post("/users", UsersController::create);
```

> **Note** : Le chemin d'une route doit systématiquement commencer par `/`.

La classe WebServerRouter permet de créer des routes pour chaque méthode HTTP :
- ``get`` pour les requêtes de type GET (récupération d'informations)
- ``post`` pour les requêtes de type POST (création d'un nouvel élément)
- `put` pour les requêtes de type PUT (mise à jour d'un élément existant)
- ``delete`` pour les requêtes de type DELETE (suppression d'un élément)

# WebServerRequest
La classe ``WebServerRequest`` fournit des informations sur la requête client en cours de traitement.

## getMethod
Fournit la méthode HTTP utilisée pour effectuer la requête
``` java
String httpMethod = context.getRequest().getMethod();
```

## getPath
Fournit le chemin de la route requêtée
``` java
String path = context.getRequest().getPath();
```
## getParam
Fournit la valeur d'un paramètre passé directement dans le chemin de la route

### Définition d'un route paramétrique
``` java
webServer.getRouter().get("/users/:userId", UsersController::find);
```

### Récupération du paramètre ``userId``
``` java
String userId = context.getRequest().getParam("userId");
```

## extractBody
La méthode ``extractBody`` est une méthode générique qui extrait les données d'un objet du corps de la requête au format JSON.
``` java
User user = context.getRequest().extractBody(User.class);
```

# WebServerResponse
La classe ``WebServerResponse`` fournit des méthodes pour répondre au client suite au traitement de sa requête.

## ok
La méthode ``ok`` renvoit le code ``200`` accompagné d'un éventuel contenu texte.
``` java
context.getResponse().ok("");
```

## json
La méthode ``json`` renvoit des données au format JSON avec le code ``200``.
``` java
final User user = new User();
context.getResponse().json(user);
```

## notFound
La méthode ``notFound`` indique que la ressource demandée n'a pas été trouvée et renvoit le code ``404`` accompagné d'un éventuel contenu texte.
``` java
context.getResponse().notFound("");
```

## serverError
La méthode ``serverError`` indique qu'une erreur s'est produite lors du traitment de la requête et renvoit le code ``500`` accompagné d'un éventuel contenu texte.
``` java
context.getResponse().serverError("");
```

# WebServerSSE
Le module ``WebServerSSE`` intégré à ``WebServer`` permet de gérer les Server Sent Events (SSE).

## emit
La méthode ``emit`` envoie des données à tous les utilisateurs d'un channel (voir ``SSEClient``).
``` java
User user = new User();
context.getSSE().emit("channel_name", user);
```

## addEventListener
Le module ``WebServerSSE`` émet des notifications lors des événements suivants :
- ``WebServerSSEEventTYpe.CONNECT`` se produit lorsqu'un utilisateur se connecte à l'API SSE du server.
- ``WebServerSSEEventType.SUBSCRIBE`` se produit lorsqu'un utilisateur s'abonne à un canal.
- ``WebServerSSEEventType.UNSUBSCRIBE`` se produit lorsqu'un utilisateur se désabonne d'un canal.

Pour être notifié de ses événements, appelez la méthode ``addEventListener`` en lui fournissant le type d'événement pour lequel vous souhaitez ête notifié et la méthode à exécuter :
``` java
webServer.getSSE().addEventListener(WebServerSSEEventType.CONNECT, callback);
```

# SSEClient

La classe ``SSEClient`` gère, côté client, la connection avec le module SSE du server Web.

## Ouverture de la connexion

``` javascript
const sseClient = new SSEClient("http://server_address:server_port");
await sseClient.connect();
```

> **Note** La méthode ``connect`` est asynchrone.


## Abonnement à un canal
La méthode subscribe le ``SSEClient`` permet de s'abonner à un canal. Lorsque le serveur diffusera un contenu sur le canal, la méthode ``callback`` passée en paramètre sera appelée.

``` javascript
await sseClient.subscribe("channel_name", callback);
```




