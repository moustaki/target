from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson
from google.appengine.ext import db
import time

class Player(db.Model):
    side = db.IntegerProperty()
    latitude = db.IntegerProperty()
    longitude = db.IntegerProperty()
    killed = db.BooleanProperty()
    def to_dict(self):
        d = {}
        d['id'] = self.key().id()
        d['latitude'] = self.latitude
        d['longitude'] = self.longitude
        d['killed'] = self.killed
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
    type = db.StringProperty()
    game_id = db.IntegerProperty()
    id_in_game = db.IntegerProperty()
    power = db.IntegerProperty()
    latitude = db.IntegerProperty()
    longitude = db.IntegerProperty()
    activated = db.BooleanProperty(default = False)
    def to_dict(self):
        d = {}
        d['type'] = self.type
        d['id'] = self.key().id()
        d['id_in_game'] = self.id_in_game
        d['power'] = self.power
        d['latitude'] = self.latitude
        d['longitude'] = self.longitude
        d['activated'] = self.activated
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
        if player:
            self.response.headers['Content-Type'] = 'application/json'
            self.response.out.write(simplejson.dumps(player.to_dict()))
        else:
            self.error(404)
            self.response.out.write('No such player')
    def post(self, player_id):
        player_id = int(player_id)
        player = Player.get_by_id(player_id)
        if player:
            self.response.headers['Content-Type'] = 'application/json'
            if self.request.get('killed'):
                if self.request.get('killed') == 'true':
                    player.killed = True
            if self.request.get('side'):
                side = int(self.request.get('side'))
                player.side = side
            if self.request.get('latitude'):
                latitude = int(self.request.get('latitude'))
                longitude = int(self.request.get('longitude'))
                player.latitude = latitude
                player.longitude = longitude
            player.put()
            self.response.out.write(simplejson.dumps(player.to_dict()))
        else:
            self.error(404)
            self.response.write('No such player')

class ActivatedObjectivesController(webapp.RequestHandler):
    def get(self):
        activated = db.GqlQuery("SELECT * FROM Objective WHERE activated = TRUE")
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps({'objectives': [ a.to_dict() for a in activated]}))

class ObjectivesShowController(webapp.RequestHandler):
    def get(self, objective_id):
        objective_id = int(objective_id)
        objective = Objective.get_by_id(objective_id)
        if objective:
            self.response.headers['Content-Type'] = 'application/json'
            self.response.out.write(simplejson.dumps(objective.to_dict()))
        else:
            self.error(404)
            self.response.out.write('No such objective')
    def post(self, objective_id):
        objective_id = int(objective_id)
        objective = Objective.get_by_id(objective_id)
        if objective:
            if self.request.get('activated') == "true":
                activated = True
            elif self.request.get('activated') == "false":
                activated = False
            objective.activated = activated
            objective.put()
            self.response.headers['Content-Type'] = 'application/json'
            self.response.out.write(simplejson.dumps(objective.to_dict())) 
        else:
            self.error(404)
            self.response.out.write('No such objective')

class ObjectivesListController(webapp.RequestHandler):
    def post(self):
        game_id = int(self.request.get('game_id'))
        game = Game.get_by_id(game_id)
        if game:
            type = self.request.get('type')
            id_in_game = int(self.request.get('id_in_game'))
            power = int(self.request.get('power'))
            latitude = int(self.request.get('latitude'))
            longitude = int(self.request.get('longitude')) 
            objective = Objective()
            objective.type = type
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
        player.killed = False
        player.put()
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps(player.to_dict()))

class StartController(webapp.RequestHandler):
    def get(self):
        game = Game()
        game.put()
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps(game.to_dict()))

class ResetController(webapp.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        keep_on = True
        try:
            while keep_on:
                q = db.GqlQuery("SELECT __key__ FROM Game")
                c1 = q.count()
                if (c1 > 0):
                    db.delete(q.fetch(200))
                q = db.GqlQuery("SELECT __key__ FROM Player")
                c2 = q.count()
                if (c2 > 0):
                    db.delete(q.fetch(200))
                q = db.GqlQuery("SELECT __key__ FROM Objective")
                c3 = q.count()
                if (c3 > 0):
                    db.delete(q.fetch(200))
                    keep_on = True
                if (c1 + c2 + c3 == 0):
                    keep_on = False
            self.response.out.write('Done\n')
        except Exception, e:
            self.response.out.write(repr(e)+'\n')
            pass


application = webapp.WSGIApplication(
                                     [
                                        ('/games/(\d+)', GameController),
                                        ('/players/(\d+)', PlayerController),
                                        ('/register', RegisterController),
                                        ('/start', StartController),
                                        ('/objectives', ObjectivesListController),
                                        ('/objectives/(\d+)', ObjectivesShowController),
                                        ('/objectives/activated', ActivatedObjectivesController),
                                        ('/reset', ResetController)
                                     ],
                                     debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
