# CinéFlix - L'Univers Disney à portée de main
CinéFlix est une application mobile Android native développée en Kotlin et Jetpack Compose. Conçue comme une plateforme de streaming, elle référence la filmographie complète de la Walt Disney Company (incluant ses acquisitions majeures : Marvel, Pixar, Star Wars, Avatar).

Au-delà d'un simple catalogue, CinéFlix intègre une dimension sociale et d'échange entre passionnés en permettant aux utilisateurs de gérer leur collection physique et numérique.

Projet réalisé par Emmy Karsenti et Ilana Coignet (Année 2025-2026).

# Fonctionnalités Principales (Requises par le projet)
L'application remplit l'intégralité du cahier des charges demandé:
- Authentification et Profil : Création de compte, connexion et page de profil dédiée.
- Catalogue par Univers : Navigation dédiée pour Marvel, Disney, Pixar, Star Wars et Avatar.
- Détails et Dates de sortie : Affichage des films avec leur date de sortie en salle, filtrables par univers et par catégories.
- Gestion de Collection Personnelle : Un utilisateur connecté peut marquer un film selon 4 statuts : Vu, À voir, Possédé, À vendre.
- Gestion depuis le Profil : L'utilisateur retrouve et gère facilement toutes ses listes de films depuis son profil.
- Base de données cloud : L'application est entièrement dynamique et connectée à Firebase (Realtime Database & Authentication).

# Fonctionnalités Bonus & Techniques (Ajouts de l'équipe)
Nous avons enrichi l'application pour offrir une expérience digne d'une véritable plateforme de SVOD et d'échange :
- Données enrichies (JSON personnalisé & API TMDB) : Modification et utilisation d'un fichier JSON complet pour recenser les films et les trier finement. Chaque film dispose de métadonnées précises : synopsis, durée, réalisateur, genre, année et franchise. L'API TMDB vient s'y greffer pour récupérer dynamiquement les affiches.
- Interface 100% Immersive (Carrousels) : Pour un rendu professionnel, l'affichage se fait sous forme de carrousels horizontaux.
  - Sur l'accueil : Carrousels dynamiques pour les "Dernières sorties", "Populaires", "Recommandations", et le tri par genres (Action, Fantastique, etc.).
  - Sur le profil : Les catégories de l'utilisateur ("Vus", "À voir", etc.) sont également présentées en carrousels pour une meilleure immersion.
- Place de marché centralisée (Achat/Vendeur) : Initialisation d'un espace d'échange via une page listant la totalité des films mis en vente par la communauté. Chaque annonce affiche l'identifiant unique (pseudo) du vendeur pour faciliter les futures interactions.
- Authentification Avancée : À l'inscription, l'utilisateur choisit un identifiant (pseudo). La connexion inclut une option "Rester connecté" (maintien de session) et il est bien sûr possible de se déconnecter depuis le profil.
- Performances & Cache : Mise en place d'un système de cache en mémoire pour les affiches afin d'éviter les rechargements inutiles et fluidifier la navigation.

# Pour aller plus loin : Perspectives d'évolution (Marché)
La fonctionnalité "Achat/Revente" remplit actuellement le critère "voir qui veut se débarrasser d'un film". Toutefois, elle a été conçue comme une base évolutive pour devenir une véritable marketplace de DVD/Blu-Ray. Voici nos objectifs pour les versions futures :
- Messagerie intégrée (Chat communautaire):
  - Objectif : Rendre le bouton "Contacter le vendeur" fonctionnel. L'application créera un salon de discussion privé dans Firebase Realtime Database entre l'acheteur potentiel et le vendeur.
- Système de tarification personnalisée
  - Objectif : Permettre à l'utilisateur, lorsqu'il clique sur "Je vends", de saisir librement son prix au lieu du prix par défaut affiché actuellement.
- Paiement In-App sécurisé
  - Objectif : Intégration de l'API Stripe pour bloquer les fonds en toute sécurité jusqu'à la remise en main propre ou l'envoi du DVD.
- Système de notation des vendeurs 
  - Objectif : Créer un climat de confiance communautaire en permettant d'évaluer une transaction (de 1 à 5 étoiles) affichée sur la carte du vendeur.
