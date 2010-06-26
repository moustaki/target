from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson
from google.appengine.ext import db

class Player(db.Model):
    side = db.IntegerProperty()
    def to_dict(self):
        d = {}
        d['id'] = self.key().id()
        if self.side:
            d['side'] = self.side
        return d
class Game(db.Model):
    players = db.ListProperty(db.Key)
    objectives = db.ListProperty(db.Key)
    def to_dict(self):
        d = {}
        d['id'] = self.key().id()
        d['players'] = []
        for player in db.get(self.players):
            d['players'].append(player.to_dict())
        d['objectives'] = []
        for objective in db.get(self.objectives):
            d['objectives'].append(objective.to_dict())
        return d
class Objective(db.Model):
    game_id = db.IntegerProperty()
    id_in_game = db.IntegerProperty()
    power = db.IntegerProperty()
    latitude = db.IntegerProperty()
    longitude = db.IntegerProperty()
    def to_dict(self):
        d = {}
        d['id'] = self.key().id()
        d['id_in_game'] = self.id_in_game
        d['power'] = self.power
        d['latitude'] = self.latitude
        d['longitude'] = self.longitude
        return d

class GameController(webapp.RequestHandler):
    def get(self, game_id):
        game_id = int(game_id)
        game = Game.get_by_id(game_id)
        if game:
            self.response.headers['Content-Type'] = 'application/json'
            self.response.out.write(simplejson.dumps(game.to_dict()))
        else:
            self.error(404)
            self.response.out.write('No such game')
    def post(self, game_id):
        game_id = int(game_id)
        game = Game.get_by_id(game_id)
        if game:
            player_id = int(self.request.get('player_id'))
            player = Player.get_by_id(player_id)
            if player:
                if player.key() not in game.players:
                    game.players.append(player.key())
                    game.put()
                self.response.headers['Content-Type'] = 'application/json'
                self.response.out.write(simplejson.dumps(game.to_dict()))
            else:
                self.error(412)
                self.response.out.write('No such player')
        else:
            self.error(404)
            self.response.out.write('No such game')

class PlayerController(webapp.RequestHandler):
    def get(self, player_id):
        player_id = int(player_id)
        player = Player.get_by_id(player_id)
        self.response.headers['Content-Type'] = 'application/json'
        if player:
            self.response.out.write(simplejson.dumps(player.to_dict()))
        else:
            self.error(404)
            self.response.out.write('No such player')
    def post(self, player_id):
        player_id = int(player_id)
        player = Player.get_by_id(player_id)
        if player:
            self.response.headers['Content-Type'] = 'application/json'
            side = int(self.request.get('side'))
            player.side = side
            player.put()
            self.response.out.write(simplejson.dumps(player.to_dict()))
        else:
            self.error(404)
            self.response.write('No such player')

class ObjectivesController(webapp.RequestHandler):
    def post(self):
        game_id = int(self.request.get('game_id'))
        game = Game.get_by_id(game_id)
        if game:
            id_in_game = int(self.request.get('id_in_game'))
            power = int(self.request.get('power'))
            latitude = int(self.request.get('latitude'))
            longitude = int(self.request.get('longitude')) 
            objective = Objective()
            objective.game_id = game_id
            objective.id_in_game = id_in_game
            objective.power = power
            objective.latitude = latitude
            objective.longitude = longitude
            objective.put()
            if objective.key() not in game.objectives:
                game.objectives.append(objective.key())
                game.put()
            self.response.headers['Content-Type'] = 'application/json'
            self.response.headers['Content-Location'] = '/objectives/' + str(objective.key().id())
            self.response.out.write(simplejson.dumps(objective.to_dict()))
        else:
            self.error(412)
            self.response.out.write('No such game')

class RegisterController(webapp.RequestHandler):
    def get(self):
        player = Player()
        player.put()
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps(player.to_dict()))

class StartController(webapp.RequestHandler):
    def get(self):
        game = Game()
        game.put()
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps(game.to_dict()))

application = webapp.WSGIApplication(
                                     [
                                        ('/games/(\d+)', GameController),
                                        ('/players/(\d+)', PlayerController),
                                        ('/register', RegisterController),
                                        ('/start', StartController),
                                        ('/objectives', ObjectivesController)
                                     ],
                                     debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
